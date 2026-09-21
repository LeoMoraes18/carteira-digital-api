package io.github.leomoraes18.carteira.dominio;

public abstract class RegraDeNegocioException extends RuntimeException {

    protected RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}