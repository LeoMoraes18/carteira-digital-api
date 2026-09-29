package io.github.leomoraes18.carteira.infra.http;

public class NotificacaoFalhouException extends RuntimeException {

    public NotificacaoFalhouException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public NotificacaoFalhouException(String mensagem) {
        super(mensagem);
    }
}