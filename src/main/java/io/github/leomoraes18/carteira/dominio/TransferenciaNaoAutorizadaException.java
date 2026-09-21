package io.github.leomoraes18.carteira.dominio;
;

public class TransferenciaNaoAutorizadaException extends RegraDeNegocioException {

    public TransferenciaNaoAutorizadaException() {
        super("transferência não autorizada pelo serviçõ autorizador");
    }
}
