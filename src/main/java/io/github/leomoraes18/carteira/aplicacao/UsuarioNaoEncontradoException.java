package io.github.leomoraes18.carteira.aplicacao;

public class UsuarioNaoEncontradoException extends RuntimeException {
    public UsuarioNaoEncontradoException(long id) {
        super("usuário não encontrado: " + id);
    }
}
