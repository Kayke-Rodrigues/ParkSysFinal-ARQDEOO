package parksys.services;

import parksys.enums.TipoVeiculo;
import parksys.exceptions.VagaOcupadaException;

// M01 - EntradaRunnable implementa Runnable para ser executado em thread separada.
// Construtor recebe: placa, TipoVeiculo, ID da vaga desejada e GerenciadorEstacionamento.
public class EntradaRunnable implements Runnable {

    private final String placa;
    private final TipoVeiculo tipo;
    private final String idVaga;
    private final GerenciadorEstacionamento gerenciador;

    public EntradaRunnable(String placa,
                           TipoVeiculo tipo,
                           String idVaga,
                           GerenciadorEstacionamento gerenciador) {
        this.placa = placa;
        this.tipo = tipo;
        this.idVaga = idVaga;
        this.gerenciador = gerenciador;
    }

    @Override
    public void run() {
        try {
            // M02 - simula tempo de processamento da entrada
            Thread.sleep(200);

            gerenciador.registrarEntrada(placa, tipo);

        } catch (InterruptedException e) {
            // M02 - restaura o flag de interrupção antes de retornar,
            // permitindo que o chamador saiba que a thread foi interrompida
            Thread.currentThread().interrupt();
            System.out.println("[EntradaRunnable] Thread interrompida: "
                    + Thread.currentThread().getName());

        } catch (VagaOcupadaException e) {
            System.out.println("[EntradaRunnable] Sem vagas para "
                    + placa + ": " + e.getMessage());
        }
    }
}
