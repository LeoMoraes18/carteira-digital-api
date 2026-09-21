package io.github.leomoraes18.carteira.dominio;

import java.math.BigDecimal;

public class SaldoInsuficienteException extends RegraDeNegocioException {

    public SaldoInsuficienteException(BigDecimal saldo, BigDecimal valor) {
        super("Saldo insuficiente: saldo %s, valor solicitado %s".formatted(saldo, valor));
    }
}
