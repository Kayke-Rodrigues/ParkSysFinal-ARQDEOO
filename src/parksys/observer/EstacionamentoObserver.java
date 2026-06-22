package parksys.observer;

import parksys.enums.StatusVaga;

// P02 - Interface do padrão Observer.
// Desacopla o GerenciadorEstacionamento das telas de UI:
// o gerenciador não precisa conhecer as classes concretas dos observadores,
// apenas que elas implementam este contrato.
public interface EstacionamentoObserver {

    // Chamado sempre que o status de uma vaga é alterado
    void onVagaAlterada(String idVaga, StatusVaga novoStatus);
}
