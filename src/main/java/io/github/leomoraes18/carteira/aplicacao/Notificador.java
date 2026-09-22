package io.github.leomoraes18.carteira.aplicacao;

@FunctionalInterface
public interface Notificador {

    void enviar(Notificacao notificacao);
}
