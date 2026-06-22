package parksys.observer;

import parksys.enums.StatusVaga;
import parksys.services.GerenciadorEstacionamento;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

// P04 - PainelMonitor recebe notificações do Observer e exibe o status das vagas.
public class PainelMonitor implements EstacionamentoObserver {

    private final GerenciadorEstacionamento gerenciador;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    public PainelMonitor(GerenciadorEstacionamento gerenciador) {
        this.gerenciador = gerenciador;
    }

    // P04 - Ao receber notificação, exibe o status atual do estacionamento
    @Override
    public void onVagaAlterada(String idVaga, StatusVaga novoStatus) {
        Map<StatusVaga, Long> contagem = gerenciador.contarVagasPorStatus();

        long livres     = contagem.getOrDefault(StatusVaga.LIVRE,      0L);
        long ocupadas   = contagem.getOrDefault(StatusVaga.OCUPADA,    0L);
        long reservadas = contagem.getOrDefault(StatusVaga.RESERVADA,  0L);

        System.out.printf("[MONITOR %s] Vaga %s -> %s | Livres: %d | Ocupadas: %d | Reservadas: %d%n",
                LocalDateTime.now().format(fmt),
                idVaga,
                novoStatus.getDescricao(),
                livres, ocupadas, reservadas);
    }
}
