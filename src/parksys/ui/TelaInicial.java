package parksys.ui;

import parksys.enums.StatusVaga;
import parksys.observer.EstacionamentoObserver;
import parksys.observer.PainelMonitor;
import parksys.services.GerenciadorArquivo;
import parksys.services.GerenciadorEstacionamento;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// P05 - MVC: TelaInicial não contém lógica de negócio.
// Toda operação delega ao GerenciadorEstacionamento via getInstance().
public class TelaInicial extends JFrame {

    private final GerenciadorEstacionamento gerenciador;
    private final EstacionamentoObserver painelMonitor;
    private JTextArea areaStatus;

    public TelaInicial() {
        // P01 - Singleton: usa sempre a mesma instância já populada pelo Principal
        gerenciador = GerenciadorEstacionamento.getInstance();

        // P06 - Registra PainelMonitor como observador ao iniciar a tela
        painelMonitor = new PainelMonitor(gerenciador) {
            @Override
            public void onVagaAlterada(String idVaga, StatusVaga novoStatus) {
                super.onVagaAlterada(idVaga, novoStatus);
                // Atualiza a área de status na UI via Event Dispatch Thread
                SwingUtilities.invokeLater(() -> atualizarAreaStatus(idVaga, novoStatus));
            }
        };
        gerenciador.adicionarObserver(painelMonitor);

        configurarJanela();
        construirUI();
    }

    private void configurarJanela() {
        setTitle("ParkSys - Sistema de Estacionamento");
        setSize(500, 420);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // S06 - Serializa automaticamente ao fechar via windowClosing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                GerenciadorArquivo.serializar(
                        gerenciador.getVagas(),
                        gerenciador.getRegistros(),
                        gerenciador.getMensalistas()
                );
                // P06 - Remove observer antes de encerrar
                gerenciador.removerObserver(painelMonitor);
                System.out.println("[ParkSys] Dados salvos. Encerrando.");
                System.exit(0);
            }
        });
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));

        // Título
        JLabel titulo = new JLabel("ParkSys", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        titulo.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        add(titulo, BorderLayout.NORTH);

        // Botões de navegação
        JPanel painelBotoes = new JPanel(new GridLayout(5, 1, 8, 8));
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));

        JButton btnEntrada    = new JButton("Registrar Entrada");
        JButton btnSaida      = new JButton("Registrar Saída");
        JButton btnMensalista = new JButton("Cadastrar Mensalista");
        JButton btnRelatorio  = new JButton("Relatório");
        JButton btnSair       = new JButton("Sair");

        estilizarBotao(btnEntrada,    new Color(46, 139, 87));
        estilizarBotao(btnSaida,      new Color(178, 34, 34));
        estilizarBotao(btnMensalista, new Color(70, 130, 180));
        estilizarBotao(btnRelatorio,  new Color(100, 100, 100));
        estilizarBotao(btnSair,       new Color(60, 60, 60));

        // P05 - MVC: botões apenas abrem as telas, sem lógica de negócio aqui
        btnEntrada.addActionListener(e -> new TelaRegistroEntrada().setVisible(true));
        btnSaida.addActionListener(e -> new TelaSaida().setVisible(true));
        btnMensalista.addActionListener(e -> new TelaCadastroMensalista().setVisible(true));
        btnRelatorio.addActionListener(e -> new TelaRelatorio().setVisible(true));
        btnSair.addActionListener(e -> {
            GerenciadorArquivo.serializar(
                    gerenciador.getVagas(),
                    gerenciador.getRegistros(),
                    gerenciador.getMensalistas()
            );
            gerenciador.removerObserver(painelMonitor);
            System.exit(0);
        });

        painelBotoes.add(btnEntrada);
        painelBotoes.add(btnSaida);
        painelBotoes.add(btnMensalista);
        painelBotoes.add(btnRelatorio);
        painelBotoes.add(btnSair);

        add(painelBotoes, BorderLayout.CENTER);

        // Área de log do Observer — mostra mudanças de status em tempo real
        areaStatus = new JTextArea(4, 40);
        areaStatus.setEditable(false);
        areaStatus.setFont(new Font("Monospaced", Font.PLAIN, 11));
        areaStatus.setBorder(BorderFactory.createTitledBorder("Monitor de Vagas"));
        JScrollPane scroll = new JScrollPane(areaStatus);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(scroll, BorderLayout.SOUTH);
    }

    private void atualizarAreaStatus(String idVaga, StatusVaga novoStatus) {
        areaStatus.append("Vaga " + idVaga + " -> " + novoStatus.getDescricao() + "\n");
        areaStatus.setCaretPosition(areaStatus.getDocument().getLength());
    }

    private void estilizarBotao(JButton botao, Color cor) {
        botao.setBackground(cor);
        botao.setForeground(Color.WHITE);
        botao.setFocusPainted(false);
        botao.setFont(new Font("Arial", Font.BOLD, 13));
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
