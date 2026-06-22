package parksys.main;

import parksys.entities.Registro;
import parksys.enums.TipoVeiculo;
import parksys.observer.PainelMonitor;
import parksys.services.*;
import parksys.ui.TelaInicial;

import javax.swing.*;
import java.util.TreeSet;

// Ponto de entrada da aplicação
public class Principal {

    public static void main(String[] args) throws InterruptedException {

        GerenciadorEstacionamento gerenciador = GerenciadorEstacionamento.getInstance();

        // S06 - Desserializa os dados da sessão anterior ao iniciar
        DadosParkSys dados = GerenciadorArquivo.desserializar();
        if (dados.getVagas() != null && !dados.getVagas().isEmpty()) {
            gerenciador.setDados(
                    dados.getVagas(),
                    dados.getRegistros(),
                    dados.getMensalistas()
            );
            System.out.println("[Principal] Dados restaurados da sessão anterior.");
        }

        // M07 - Após desserializar, threadOrigem de todos os registros é null.
        // Campos transient não fazem parte da serialização Java: ao gravar,
        // são ignorados; ao restaurar, recebem o valor padrão (null para String).
        // A thread que originou cada entrada não existe mais após reiniciar o sistema.
        System.out.println("\n[M07] threadOrigem após desserialização:");
        for (Registro r : gerenciador.getRegistros()) {
            System.out.println("  Placa " + r.getVeiculo().getPlaca()
                    + " | threadOrigem = " + r.getThreadOrigem()
                    + " (transient -> null esperado)");
        }

        // P06 - Registra PainelMonitor como observador antes de qualquer operação
        PainelMonitor painelMonitor = new PainelMonitor(gerenciador);
        gerenciador.adicionarObserver(painelMonitor);

        // M06 - MonitorRunnable como thread daemon
        // Daemon threads são finalizadas automaticamente quando a JVM encerra
        MonitorRunnable monitorRunnable = new MonitorRunnable(gerenciador);
        Thread threadMonitor = new Thread(monitorRunnable, "Monitor-Daemon");
        threadMonitor.setDaemon(true); // deve ser chamado ANTES do start()
        threadMonitor.start();

        // M05 - Mínimo de 4 threads de entrada com nomes descritivos
        // M01 - EntradaRunnable recebe placa, tipo, idVaga e gerenciador
        Thread t1 = new Thread(new EntradaRunnable("ABC-1234", TipoVeiculo.CARRO,    "A01", gerenciador), "Entrada-1");
        Thread t2 = new Thread(new EntradaRunnable("DEF-5678", TipoVeiculo.MOTO,     "A02", gerenciador), "Entrada-2");
        Thread t3 = new Thread(new EntradaRunnable("GHI-9012", TipoVeiculo.SUV,      "B01", gerenciador), "Entrada-3");
        Thread t4 = new Thread(new EntradaRunnable("JKL-3456", TipoVeiculo.CAMINHAO, "B03", gerenciador), "Entrada-4");

        t1.start(); t2.start(); t3.start(); t4.start();

        // M05 - join() aguarda todas as threads antes de prosseguir
        t1.join(); t2.join(); t3.join(); t4.join();

        System.out.println("\n[Principal] Todas as entradas processadas.");

        // C04 - Registros em ordem cronológica via TreeSet (usa Comparable)
        System.out.println("\n=== Registros ordenados (TreeSet / Comparable) ===");
        TreeSet<Registro> ordenados = gerenciador.getRegistrosOrdenados();
        for (Registro r : ordenados) {
            System.out.println("  " + r.getVeiculo().getPlaca()
                    + " | Thread: " + r.getThreadOrigem()
                    + " | Vaga: " + r.getIdVaga());
        }

        // Abre a interface gráfica Swing na Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            TelaInicial tela = new TelaInicial();
            tela.setVisible(true);
        });
    }
}
