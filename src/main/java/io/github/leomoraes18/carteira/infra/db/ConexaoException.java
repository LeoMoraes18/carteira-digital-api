package io.github.leomoraes18.carteira.infra.db;

public class ConexaoException extends RuntimeException {
    public ConexaoException(String message, Throwable causa) {
        super(message, causa);
    }
}
