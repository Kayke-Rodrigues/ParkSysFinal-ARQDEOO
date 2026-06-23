package parksys.services;

import java.util.*;
import java.util.stream.Collectors;

import parksys.entities.*;
import parksys.enums.StatusVaga;
import parksys.enums.TipoVeiculo;
import parksys.exceptions.VagaOcupadaException;
import parksys.exceptions.VeiculoNaoEncontradoException;
import parksys.observer.EstacionamentoObserver;

import java.time.Duration;
import java.time.LocalDateTime;

public class GerenciadorEstacionamento {

    // P01 - Singleton: construtor privado com instância estática
    private static GerenciadorEstacionamento instance;

    // C01 - HashMap para acesso O(1) por ID da vaga
    private HashMap<String, Vaga> vagas;

    // C02 - ArrayList mantem ordem de chegada dos registros
    private ArrayList<Registro> registros;

    // C03 - LinkedList é a melgor para mensalistas pois inserção e remoção
    // frequente nas pontas (add/remove) tem custo O(1), sem precisar de
    // deslocamento de elementos como ocorreria em ArrayList
    private LinkedList<Mensalista> mensalistas;

    // P03 - Lista de observadores
    private ArrayList<EstacionamentoObserver> observers;

    private GerenciadorEstacionamento() {
        vagas = new HashMap<>();
        registros = new ArrayList<>();
        mensalistas = new LinkedList<>();
        observers = new ArrayList<>();
        inicializarVagas();
    }

    // P01 - Singleton: método público que retorna a mesma instância
    public static GerenciadorEstacionamento getInstance() {
        if (instance == null) {
            instance = new GerenciadorEstacionamento();
        }
        return instance;
    }

    // Permite restaurar a instância após desserialização
    public static void setInstance(GerenciadorEstacionamento instancia) {
        instance = instancia;
    }

    private void inicializarVagas() {
        for (int i = 1; i <= 15; i++) {
            String vagaA = String.format("A%02d", i);
            String vagaB = String.format("B%02d", i);
            vagas.put(vagaA, new Vaga(vagaA));
            vagas.put(vagaB, new Vaga(vagaB));
        }
    }

    public HashMap<String, Vaga> getVagas() {
        return vagas;
    }

    public ArrayList<Registro> getRegistros() {
        return registros;
    }

    public LinkedList<Mensalista> getMensalistas() {
        return mensalistas;
    }

   
     // M03 - synchronized evita a race condition: sem essa proteção, duas threads
    // poderiam verificar a disponibilidade da mesma vaga simultaneamente e
    // ambas enxergariam LIVRE, resultando em dupla ocupação.
    // T03/T04 - Usa getVagasOcupadas() do enum para saber quantas vagas consecutivas alocar
    public synchronized void registrarEntrada(String placa, TipoVeiculo tipo)
            throws VagaOcupadaException {

        // Valida mensalista: se a placa tem vaga reservada, usa ela
        Mensalista mensalista = buscarMensalista(placa);
        if (mensalista != null) {
            String idReservada = mensalista.getVagaReservada();
            Vaga vagaReservada = vagas.get(idReservada);
            if (vagaReservada != null && vagaReservada.getStatus() == StatusVaga.RESERVADA) {
                vagaReservada.setStatus(StatusVaga.OCUPADA);
                Registro registro = new Registro(new Veiculo(placa, tipo), idReservada);
                // M04 - grava o nome da thread que processou a entrada
                registro.setThreadOrigem(Thread.currentThread().getName());
                registros.add(registro);
                notificarObservers(idReservada, StatusVaga.OCUPADA);
                System.out.println("Mensalista estacionado na vaga reservada: " + idReservada);
                return;
            }
        }

        // T04 - verifica quantas vagas consecutivas o veículo precisa
        int vagasNecessarias = tipo.getVagasOcupadas();

        if (vagasNecessarias == 1) {
            for (Vaga vaga : vagas.values()) {
                if (vaga.getStatus() == StatusVaga.LIVRE) {
                    vaga.setStatus(StatusVaga.OCUPADA);
                    Registro registro = new Registro(new Veiculo(placa, tipo), vaga.getId());
                    registro.setThreadOrigem(Thread.currentThread().getName());
                    registros.add(registro);
                    notificarObservers(vaga.getId(), StatusVaga.OCUPADA);
                    System.out.println("Veículo " + placa + " estacionado na vaga: " + vaga.getId());
                    return;
                }
            }
        } else {
            // SUV (2 vagas) e CAMINHAO (3 vagas): busca vagas consecutivas na mesma fileira
            List<String> vagasConsecutivas = buscarVagasConsecutivas(vagasNecessarias);

            if (vagasConsecutivas != null) {
                String idPrincipal = vagasConsecutivas.get(0);
                for (String idVaga : vagasConsecutivas) {
                    vagas.get(idVaga).setStatus(StatusVaga.OCUPADA);
                    notificarObservers(idVaga, StatusVaga.OCUPADA);
                }
                Registro registro = new Registro(new Veiculo(placa, tipo), idPrincipal);
                registro.setThreadOrigem(Thread.currentThread().getName());
                registro.setVagasExtras(new ArrayList<>(vagasConsecutivas.subList(1, vagasConsecutivas.size())));
                registros.add(registro);
                System.out.println("Veículo " + placa + " (" + tipo.getNome()
                        + ") estacionado nas vagas: " + String.join(", ", vagasConsecutivas));
                return;
            }
        }

        throw new VagaOcupadaException("Não há vagas disponíveis para " + tipo.getNome() + ".");
    }

    // Busca vagas consecutivas livres na mesma fileira (A ou B)
    private List<String> buscarVagasConsecutivas(int quantidade) {
        String[] fileiras = {"A", "B"};
        for (String fileira : fileiras) {
            List<String> sequencia = new ArrayList<>();
            for (int i = 1; i <= 15; i++) {
                String idVaga = String.format("%s%02d", fileira, i);
                Vaga vaga = vagas.get(idVaga);
                if (vaga != null && vaga.getStatus() == StatusVaga.LIVRE) {
                    sequencia.add(idVaga);
                    if (sequencia.size() == quantidade) return sequencia;
                } else {
                    sequencia.clear();
                }
            }
        }
        return null;
    }


    // M03 - synchronized evita alterações simultâneas na lista de registros e no mapa de vagas

    public synchronized void registrarSaida(String placa) throws VeiculoNaoEncontradoException {

        for (Registro registro : registros) {
            if (registro.getVeiculo().getPlaca().equalsIgnoreCase(placa)
                    && registro.getDataSaida() == null) {

                registro.setDataSaida(LocalDateTime.now());

                long horas = Duration.between(
                        registro.getDataEntrada(),
                        registro.getDataSaida()
                ).toHours();

                if (horas == 0) horas = 1;

                // T03 - usa getTarifaHora() do enum, sem valor fixo no código
                double valor = horas * registro.getVeiculo().getTipo().getTarifaHora();
                registro.setValorPago(valor);

                // Libera vaga principal
                Vaga vaga = vagas.get(registro.getIdVaga());
                if (vaga != null) {
                    vaga.setStatus(StatusVaga.LIVRE);
                    notificarObservers(registro.getIdVaga(), StatusVaga.LIVRE);
                }

                // Libera vagas extras 
                if (registro.getVagasExtras() != null) {
                    for (String idExtra : registro.getVagasExtras()) {
                        Vaga vagaExtra = vagas.get(idExtra);
                        if (vagaExtra != null) {
                            vagaExtra.setStatus(StatusVaga.LIVRE);
                            notificarObservers(idExtra, StatusVaga.LIVRE);
                        }
                    }
                }

                System.out.println("Saída registrada! Valor: R$ " + valor);
                return;
            }
        }

        throw new VeiculoNaoEncontradoException("Veículo com placa " + placa + " não encontrado.");
    }

    public synchronized void cadastrarMensalista(Mensalista mensalista) {
        mensalistas.add(mensalista);
        Vaga vaga = vagas.get(mensalista.getVagaReservada());
        if (vaga != null) {
            vaga.setStatus(StatusVaga.RESERVADA);
            notificarObservers(mensalista.getVagaReservada(), StatusVaga.RESERVADA);
        }
    }

    public Mensalista buscarMensalista(String placa) {
        for (Mensalista m : mensalistas) {
            if (m.getPlaca().equalsIgnoreCase(placa)) return m;
        }
        return null;
    }

    public synchronized boolean removerMensalista(String placa) {
        Mensalista mensalista = buscarMensalista(placa);
        if (mensalista != null) {
            Vaga vaga = vagas.get(mensalista.getVagaReservada());
            if (vaga != null && vaga.getStatus() == StatusVaga.RESERVADA) {
                vaga.setStatus(StatusVaga.LIVRE);
                notificarObservers(mensalista.getVagaReservada(), StatusVaga.LIVRE);
            }
            mensalistas.remove(mensalista);
            return true;
        }
        return false;
    }

    // C04 - TreeSet usa o Comparable<Registro> (compareTo por dataEntrada)
    // para retornar registros automaticamente em ordem 
    public TreeSet<Registro> getRegistrosOrdenados() {
        return new TreeSet<>(registros);
    }

    // C05 - Comparator para ordenar por valor pago (decrescente).
    // Comparable define ordem natural (dataEntrada); Comparator define
    // ordem alternativa sem alterar a classe — ideal para relatórios específicos.
    public List<Registro> getRegistrosOrdenadosPorReceita() {
        return registros.stream()
                .filter(r -> r.getDataSaida() != null)
                .sorted(Comparator.comparingDouble(Registro::getValorPago).reversed())
                .collect(Collectors.toList());
    }

    // Utilitário para contagem de vagas por status
    public Map<StatusVaga, Long> contarVagasPorStatus() {
        return vagas.values().stream()
                .collect(Collectors.groupingBy(Vaga::getStatus, Collectors.counting()));
    }

    // P03 - Observer
    public void adicionarObserver(EstacionamentoObserver observer) {
        observers.add(observer);
    }

    public void removerObserver(EstacionamentoObserver observer) {
        observers.remove(observer);
    }

    // P02 - Notifica com idVaga e novo status
    private void notificarObservers(String idVaga, StatusVaga novoStatus) {
        for (EstacionamentoObserver observer : observers) {
            observer.onVagaAlterada(idVaga, novoStatus);
        }
    }

    // Permite repopular os dados após desserialização
    public void setDados(HashMap<String, Vaga> vagas,
                         ArrayList<Registro> registros,
                         LinkedList<Mensalista> mensalistas) {
        this.vagas = vagas;
        this.registros = registros;
        this.mensalistas = mensalistas;
    }
}
