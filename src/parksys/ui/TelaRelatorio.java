package parksys.ui;

import parksys.entities.Registro;
import parksys.entities.Vaga;
import parksys.enums.StatusVaga;
import parksys.services.GerenciadorArquivo;
import parksys.services.GerenciadorEstacionamento;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

// P05 - MVC: TelaRelatorio apenas exibe dados recebidos do gerenciador.
// C06 - Usa entrySet() do HashMap, for-each sobre TreeSet e Comparator para receita.
public class TelaRelatorio extends JFrame {

    private final GerenciadorEstacionamento gerenciador;
    private JTextArea areaRelatorio;

    public TelaRelatorio() {
        gerenciador = GerenciadorEstacionamento.getInstance();
        configurarJanela();
        construirUI();
        carregarRelatorio();
    }

    private void configurarJanela() {
        setTitle("Relatório");
        setSize(650, 550);
        setLocationRelativeTo(null);
    }

    private void construirUI() {
        setLayout(new BorderLayout(5, 5));

        areaRelatorio = new JTextArea();
        areaRelatorio.setEditable(false);
        areaRelatorio.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(areaRelatorio), BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnAtualizar = new JButton("Atualizar");
        btnAtualizar.addActionListener(e -> carregarRelatorio());

        JButton btnExportar = new JButton("Exportar .txt");
        btnExportar.addActionListener(e -> exportarTxt());

        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnExportar);
        add(painelBotoes, BorderLayout.SOUTH);
    }

    private void carregarRelatorio() {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDate hoje = LocalDate.now();

        // C06 - Contagem de vagas usando entrySet() do HashMap
        sb.append("======== STATUS DAS VAGAS ========\n");
        long livres = 0, ocupadas = 0, reservadas = 0;

        // Itera sobre entrySet() do HashMap conforme requisito C06
        for (Map.Entry<String, Vaga> entry : gerenciador.getVagas().entrySet()) {
            StatusVaga status = entry.getValue().getStatus();
            if (status == StatusVaga.LIVRE)           livres++;
            else if (status == StatusVaga.OCUPADA)    ocupadas++;
            else if (status == StatusVaga.RESERVADA)  reservadas++;
        }

        sb.append(String.format("Livres: %d | Ocupadas: %d | Reservadas: %d\n\n",
                livres, ocupadas, reservadas));

        // C06 - Listagem de registros do dia usando filtro por data
        sb.append("======== REGISTROS DO DIA (" + hoje.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ") ========\n");
        List<Registro> registrosDoDia = gerenciador.getRegistros().stream()
                .filter(r -> r.getDataEntrada().toLocalDate().equals(hoje))
                .collect(Collectors.toList());

        if (registrosDoDia.isEmpty()) {
            sb.append("Nenhum registro hoje.\n");
        } else {
            for (Registro r : registrosDoDia) {
                sb.append(String.format("%-10s | %-18s | Vaga: %-4s | Entrada: %s",
                        r.getVeiculo().getPlaca(),
                        r.getVeiculo().getTipo().getNome(),
                        r.getIdVaga(),
                        r.getDataEntrada().format(fmt)));
                if (r.getDataSaida() != null) {
                    sb.append(String.format(" | Saída: %s | R$ %.2f",
                            r.getDataSaida().format(fmt), r.getValorPago()));
                } else {
                    sb.append(" | (no estacionamento)");
                }
                sb.append("\n");
            }
        }

        // C04 - Todos os registros em ordem cronológica via TreeSet (Comparable)
        sb.append("\n======== TODOS OS REGISTROS (ordem cronológica) ========\n");
        TreeSet<Registro> ordenados = gerenciador.getRegistrosOrdenados();
        for (Registro r : ordenados) {
            sb.append(String.format("%-10s | %-18s | Vaga: %-4s | %s\n",
                    r.getVeiculo().getPlaca(),
                    r.getVeiculo().getTipo().getNome(),
                    r.getIdVaga(),
                    r.getDataEntrada().format(fmt)));
        }

        // C05 - Relatório de receita ordenado por valor decrescente via Comparator
        // Diferença entre Comparable e Comparator:
        // Comparable (Registro.compareTo) define a ordem NATURAL da classe (por dataEntrada).
        // Comparator define uma ordem ALTERNATIVA sem alterar a classe,
        // ideal para ordenações específicas como receita decrescente.
        sb.append("\n======== RECEITA (maior para menor) ========\n");
        List<Registro> porReceita = gerenciador.getRegistrosOrdenadosPorReceita();
        double totalReceita = 0;
        for (Registro r : porReceita) {
            sb.append(String.format("%-10s | R$ %8.2f\n",
                    r.getVeiculo().getPlaca(), r.getValorPago()));
            totalReceita += r.getValorPago();
        }
        sb.append(String.format("\nRECEITA TOTAL: R$ %.2f\n", totalReceita));

        areaRelatorio.setText(sb.toString());
        areaRelatorio.setCaretPosition(0);
    }

    private void exportarTxt() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("relatorio_parksys.txt"));
        int resultado = chooser.showSaveDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            String caminho = chooser.getSelectedFile().getAbsolutePath();
            GerenciadorArquivo.exportarRelatorioTxt(gerenciador.getRegistros(), caminho);
            JOptionPane.showMessageDialog(this,
                    "Relatório exportado:\n" + caminho,
                    "Exportado", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
