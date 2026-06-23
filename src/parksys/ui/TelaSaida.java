package parksys.ui;

import parksys.exceptions.VeiculoNaoEncontradoException;
import parksys.services.GerenciadorArquivo;
import parksys.services.GerenciadorEstacionamento;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// P05 - MVC: sem lógica de negócio aqui.
public class TelaSaida extends JFrame {

    private final GerenciadorEstacionamento gerenciador;
    private JTextField campPlaca;

    public TelaSaida() {
        gerenciador = GerenciadorEstacionamento.getInstance();
        configurarJanela();
        construirUI();
    }

    private void configurarJanela() {
        setTitle("Registrar Saída");
        setSize(360, 170);
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
        painel.add(new JLabel("Placa do Veículo:"), gbc);
        gbc.gridx = 1;
        campPlaca = new JTextField(15);
        painel.add(campPlaca, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        JButton btnSaida = new JButton("Registrar Saída");
        btnSaida.setBackground(new Color(178, 34, 34));
        btnSaida.setForeground(Color.WHITE);
        btnSaida.setFocusPainted(false);
        btnSaida.setFont(new Font("Arial", Font.BOLD, 13));
        btnSaida.addActionListener(e -> registrarSaida());
        painel.add(btnSaida, gbc);

        add(painel);
    }

    private void registrarSaida() {
        String placa = campPlaca.getText().trim().toUpperCase();

        if (placa.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Informe a placa.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            gerenciador.registrarSaida(placa);
            JOptionPane.showMessageDialog(this,
                    "Saída registrada para: " + placa,
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            campPlaca.setText("");

        } catch (VeiculoNaoEncontradoException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Não encontrado", JOptionPane.ERROR_MESSAGE);
        }
    }
}
