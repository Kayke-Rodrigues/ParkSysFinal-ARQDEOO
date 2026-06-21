package parksys.services;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import parksys.entities.Mensalista;
import parksys.entities.Registro;
import parksys.entities.Vaga;

public class GerenciadorArquivo {

    private static final String CAMINHO_PADRAO = "parksys_dados.ser";

    // S02 - Serializa os três mapas em um único arquivo usando ObjectOutputStream.
    // O bloco finally garante que o log de resultado sempre seja exibido,
    // mesmo que ocorra uma exceção no meio da operação.
    public static void serializar(HashMap<String, Vaga> vagas,
                                  ArrayList<Registro> registros,
                                  LinkedList<Mensalista> mensalistas,
                                  String path) {
        ObjectOutputStream oos = null;
        boolean sucesso = false;

        try {
            oos = new ObjectOutputStream(new FileOutputStream(path));
            DadosParkSys dados = new DadosParkSys(vagas, registros, mensalistas);
            oos.writeObject(dados);
            sucesso = true;

        } catch (IOException e) {
            // S05 - IOException tratada
            System.err.println("[GerenciadorArquivo] Erro ao serializar: " + e.getMessage());

        } finally {
            // S05 - finally sempre loga o resultado da operação
            if (oos != null) {
                try { oos.close(); } catch (IOException e) {
                    System.err.println("[GerenciadorArquivo] Erro ao fechar stream: " + e.getMessage());
                }
            }
            System.out.println("[GerenciadorArquivo] Serialização: " + (sucesso ? "OK" : "FALHOU"));
        }
    }

    public static void serializar(HashMap<String, Vaga> vagas,
                                  ArrayList<Registro> registros,
                                  LinkedList<Mensalista> mensalistas) {
        serializar(vagas, registros, mensalistas, CAMINHO_PADRAO);
    }

    // S03 - Desserializa e retorna DadosParkSys.
    // Se o arquivo não existir, inicializa estruturas vazias.
    public static DadosParkSys desserializar(String path) {
        ObjectInputStream ois = null;
        DadosParkSys dados = null;
        boolean sucesso = false;

        try {
            ois = new ObjectInputStream(new FileInputStream(path));
            dados = (DadosParkSys) ois.readObject();
            sucesso = true;

        } catch (FileNotFoundException e) {
            // Primeira execução: arquivo ainda não existe
            System.out.println("[GerenciadorArquivo] Arquivo não encontrado. Iniciando com dados vazios.");
            dados = new DadosParkSys(new HashMap<>(), new ArrayList<>(), new LinkedList<>());

        } catch (ClassNotFoundException e) {
            // S05 - ClassNotFoundException tratada separadamente
            System.err.println("[GerenciadorArquivo] Classe não encontrada: " + e.getMessage());
            dados = new DadosParkSys(new HashMap<>(), new ArrayList<>(), new LinkedList<>());

        } catch (IOException e) {
            // S05 - IOException tratada
            System.err.println("[GerenciadorArquivo] Erro de I/O: " + e.getMessage());
            dados = new DadosParkSys(new HashMap<>(), new ArrayList<>(), new LinkedList<>());

        } finally {
            if (ois != null) {
                try { ois.close(); } catch (IOException e) {
                    System.err.println("[GerenciadorArquivo] Erro ao fechar stream: " + e.getMessage());
                }
            }
            // S05 - finally sempre loga o resultado
            System.out.println("[GerenciadorArquivo] Desserialização: "
                    + (sucesso ? "OK" : "Dados inicializados vazios"));
        }

        return dados;
    }

    public static DadosParkSys desserializar() {
        return desserializar(CAMINHO_PADRAO);
    }

    // S04 - Exporta relatório em .txt com BufferedWriter
    public static void exportarRelatorioTxt(List<Registro> registros, String path) {
        BufferedWriter writer = null;
        boolean sucesso = false;

        try {
            writer = new BufferedWriter(new FileWriter(path));
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            writer.write("========================================");
            writer.newLine();
            writer.write("       RELATÓRIO DE ESTACIONAMENTO      ");
            writer.newLine();
            writer.write("  ParkSys - " + LocalDateTime.now().format(fmt));
            writer.newLine();
            writer.write("========================================");
            writer.newLine();
            writer.newLine();

            double totalReceita = 0;
            int totalRegistros = 0;

            for (Registro r : registros) {
                writer.write("Placa    : " + r.getVeiculo().getPlaca());
                writer.newLine();
                writer.write("Tipo     : " + r.getVeiculo().getTipo().getNome());
                writer.newLine();
                writer.write("Vaga     : " + r.getIdVaga());
                writer.newLine();
                writer.write("Entrada  : " + r.getDataEntrada().format(fmt));
                writer.newLine();

                if (r.getDataSaida() != null) {
                    writer.write("Saída    : " + r.getDataSaida().format(fmt));
                    writer.newLine();
                    writer.write(String.format("Valor    : R$ %.2f", r.getValorPago()));
                    writer.newLine();
                    totalReceita += r.getValorPago();
                } else {
                    writer.write("Saída    : (veículo ainda no estacionamento)");
                    writer.newLine();
                }

                writer.write("----------------------------------------");
                writer.newLine();
                totalRegistros++;
            }

            writer.newLine();
            writer.write("TOTAL DE REGISTROS : " + totalRegistros);
            writer.newLine();
            writer.write(String.format("RECEITA TOTAL      : R$ %.2f", totalReceita));
            writer.newLine();
            writer.write("========================================");
            writer.newLine();
            sucesso = true;

        } catch (IOException e) {
            // S05 - IOException tratada
            System.err.println("[GerenciadorArquivo] Erro ao exportar relatório: " + e.getMessage());

        } finally {
            if (writer != null) {
                try { writer.close(); } catch (IOException e) {
                    System.err.println("[GerenciadorArquivo] Erro ao fechar writer: " + e.getMessage());
                }
            }
            // S05 - finally sempre loga o resultado
            System.out.println("[GerenciadorArquivo] Exportação: " + (sucesso ? "OK -> " + path : "FALHOU"));
        }
    }
}
