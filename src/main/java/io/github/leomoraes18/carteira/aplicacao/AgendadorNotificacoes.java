package io.github.leomoraes18.carteira.aplicacao;

@FunctionalInterface
public interface AgendadorNotificacoes {

    void agendar(Notificacao notificacao);
}
