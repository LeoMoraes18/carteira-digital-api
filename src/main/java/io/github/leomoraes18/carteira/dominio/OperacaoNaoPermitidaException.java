package io.github.leomoraes18.carteira.dominio;

public class OperacaoNaoPermitidaException extends RegraDeNegocioException {

    public OperacaoNaoPermitidaException(String mensagem) {
        super(mensagem);
    }
}
