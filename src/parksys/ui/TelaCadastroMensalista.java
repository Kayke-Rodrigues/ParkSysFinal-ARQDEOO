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
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        painel.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1;
        campNome = new JTextField(18);
        painel.add(campNome, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        painel.add(new JLabel("Placa:"), gbc);
        gbc.gridx = 1;
        campPlaca = new JTextField(18);
        painel.add(campPlaca, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        painel.add(new JLabel("Vaga Reservada (ex: A05):"), gbc);
        gbc.gridx = 1;
        campVaga = new JTextField(18);
        painel.add(campVaga, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JButton btnCadastrar = new JButton("Cadastrar");
        btnCadastrar.setBackground(new Color(70, 130, 180));
        btnCadastrar.setForeground(Color.WHITE);
        btnCadastrar.setFocusPainted(false);
        btnCadastrar.setFont(new Font("Arial", Font.BOLD, 13));
        btnCadastrar.addActionListener(e -> cadastrar());
        painel.add(btnCadastrar, gbc);

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
