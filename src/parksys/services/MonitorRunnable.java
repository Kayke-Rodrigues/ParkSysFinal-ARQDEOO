package parksys.services;

import parksys.enums.StatusVaga;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

// M06 - MonitorRunnable é uma thread daemon: ela roda em background enquanto
// o sistema está ativo e é encerrada automaticamente quando todas as threads
// não-daemon terminam (sem necessidade de encerramento explícito).
public class MonitorRunnable implements Runnable {

    private final GerenciadorEstacionamento gerenciador;
    private volatile boolean ativo = true;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    public MonitorRunnable(GerenciadorEstacionamento gerenciador) {
        this.gerenciador = gerenciador;
    }

    @Override
    public void run() {
        System.out.println("[Monitor] Iniciado como thread daemon.");

        while (ativo) {
            try {
                Thread.sleep(1000);

                Map<StatusVaga, Long> contagem = gerenciador.contarVagasPorStatus();

                long livres    = contagem.getOrDefault(StatusVaga.LIVRE,      0L);
                long ocupadas  = contagem.getOrDefault(StatusVaga.OCUPADA,    0L);
                long reservadas = contagem.getOrDefault(StatusVaga.RESERVADA, 0L);

                System.out.printf("[Monitor %s] Livres: %d | Ocupadas: %d | Reservadas: %d%n",
                        LocalDateTime.now().format(fmt), livres, ocupadas, reservadas);

            } catch (InterruptedException e) {
                // M02 - restaura o flag e encerra o loop de forma limpa
                Thread.currentThread().interrupt();
                ativo = false;
                System.out.println("[Monitor] Encerrado por interrupção.");
            }
        }
    }

    public void encerrar() {
        ativo = false;
    }
}