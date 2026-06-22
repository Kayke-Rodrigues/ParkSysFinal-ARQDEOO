package parksys.ui;

import parksys.entities.Mensalista;
import parksys.services.GerenciadorArquivo;
import parksys.services.GerenciadorEstacionamento;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// P05 - MVC: sem lógica de negócio aqui.
public class TelaCadastroMensalista extends JFrame {

    private final GerenciadorEstacionamento gerenciador;
    private JTextField campNome;
    private JTextField campPlaca;
    private JTextField campVaga;

    public TelaCadastroMensalista() {
        gerenciador = GerenciadorEstacionamento.getInstance();
        configurarJanela();
        construirUI();
    }

    private void configurarJanela() {
        setTitle("Cadastrar Mensalista");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setResizable(false);

        // S06 - serializa ao fechar
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                GerenciadorArquivo.serializar(
                        gerenciador.getVagas(),
                        gerenciador.getRegistros(),
                        gerenciador.getMensalistas()
                );
                dispose();
            }
        });
    }

    private void construirUI() {
    JPanel painel = new JPanel(new GridLayout(4, 2, 10, 15));
    painel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

    JLabel lblNome = new JLabel("Nome:");
    lblNome.setFont(new Font("Arial", Font.PLAIN, 14));
    campNome = new JTextField();
    campNome.setFont(new Font("Arial", Font.PLAIN, 14));

    JLabel lblPlaca = new JLabel("Placa:");
    lblPlaca.setFont(new Font("Arial", Font.PLAIN, 14));
    campPlaca = new JTextField();
    campPlaca.setFont(new Font("Arial", Font.PLAIN, 14));

    JLabel lblVaga = new JLabel("Vaga (ex: A05):");
    lblVaga.setFont(new Font("Arial", Font.PLAIN, 14));
    campVaga = new JTextField();
    campVaga.setFont(new Font("Arial", Font.PLAIN, 14));

    JLabel lblVazio = new JLabel("");
    JButton btnCadastrar = new JButton("Cadastrar");
    btnCadastrar.setBackground(new Color(70, 130, 180));
    btnCadastrar.setForeground(Color.WHITE);
    btnCadastrar.setFocusPainted(false);
    btnCadastrar.setFont(new Font("Arial", Font.BOLD, 14));
    btnCadastrar.addActionListener(e -> cadastrar());

    painel.add(lblNome);
    painel.add(campNome);
    painel.add(lblPlaca);
    painel.add(campPlaca);
    painel.add(lblVaga);
    painel.add(campVaga);
    painel.add(lblVazio);
    painel.add(btnCadastrar);

    add(painel);
}
    private void cadastrar() {
        String nome  = campNome.getText().trim();
        String placa = campPlaca.getText().trim().toUpperCase();
        String vaga  = campVaga.getText().trim().toUpperCase();

        if (nome.isEmpty() || placa.isEmpty() || vaga.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Preencha todos os campos.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (gerenciador.getVagas().get(vaga) == null) {
            JOptionPane.showMessageDialog(this,
                    "Vaga '" + vaga + "' não existe. Use o formato A01–A15 ou B01–B15.",
                    "Vaga inválida", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Mensalista mensalista = new Mensalista(nome, placa, vaga);
        gerenciador.cadastrarMensalista(mensalista);

        JOptionPane.showMessageDialog(this,
                "Mensalista cadastrado!\n" + nome + " - Vaga: " + vaga,
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);

        campNome.setText("");
        campPlaca.setText("");
        campVaga.setText("");
    }
}
