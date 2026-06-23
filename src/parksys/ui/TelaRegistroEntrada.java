package parksys.ui;

import parksys.enums.TipoVeiculo;
import parksys.exceptions.VagaOcupadaException;
import parksys.services.GerenciadorArquivo;
import parksys.services.GerenciadorEstacionamento;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// P05 - MVC: sem lógica de negócio. Delega ao GerenciadorEstacionamento.
public class TelaRegistroEntrada extends JFrame {

    private final GerenciadorEstacionamento gerenciador;
    private JTextField campPlaca;
    private JComboBox<TipoVeiculo> comboTipo;

    public TelaRegistroEntrada() {
        gerenciador = GerenciadorEstacionamento.getInstance();
        configurarJanela();
        construirUI();
    }

    private void configurarJanela() {
        setTitle("Registrar Entrada");
        setSize(380, 220);
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
        painel.add(new JLabel("Placa:"), gbc);
        gbc.gridx = 1;
        campPlaca = new JTextField(15);
        painel.add(campPlaca, gbc);

        // T05 - ComboBox preenchido dinamicamente com TipoVeiculo.values()
        // Nunca usa Strings fixas — itera sobre os valores do enum
        gbc.gridx = 0; gbc.gridy = 1;
        painel.add(new JLabel("Tipo de Veículo:"), gbc);
        gbc.gridx = 1;
        comboTipo = new JComboBox<>(TipoVeiculo.values());
        comboTipo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TipoVeiculo) {
                    setText(((TipoVeiculo) value).getNome());
                }
                return this;
            }
        });
        painel.add(comboTipo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton btnRegistrar = new JButton("Registrar Entrada");
        btnRegistrar.setBackground(new Color(46, 139, 87));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.setFont(new Font("Arial", Font.BOLD, 13));
        btnRegistrar.addActionListener(e -> registrarEntrada());
        painel.add(btnRegistrar, gbc);

        add(painel);
    }

    private void registrarEntrada() {
        String placa = campPlaca.getText().trim().toUpperCase();
        TipoVeiculo tipo = (TipoVeiculo) comboTipo.getSelectedItem();

        if (placa.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Informe a placa do veículo.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            gerenciador.registrarEntrada(placa, tipo);
            JOptionPane.showMessageDialog(this,
                    "Entrada registrada!\nPlaca: " + placa + "\nTipo: " + tipo.getNome(),
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            campPlaca.setText("");

        } catch (VagaOcupadaException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Sem vagas", JOptionPane.WARNING_MESSAGE);
        }
    }
}
