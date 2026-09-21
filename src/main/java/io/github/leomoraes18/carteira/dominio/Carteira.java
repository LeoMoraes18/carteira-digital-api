package io.github.leomoraes18.carteira.dominio;

import java.math.BigDecimal;
import java.util.Objects;

public class Carteira {

    private BigDecimal saldo;

    public Carteira(BigDecimal saldoInicial) {
        Objects.requireNonNull(saldoInicial, "saldo inicial é obrigatório");
        if (saldoInicial.signum() < 0) {
            throw new IllegalArgumentException("saldo inicial não pode ser negativo");
        }
        this.saldo = saldoInicial;
    }

    public void validarDebito(BigDecimal valor) {
        validarValorPositivo(valor);
        if (saldo.compareTo(valor) < 0) {
            throw new SaldoInsuficienteException(saldo, valor);
        }
    }

    public void debitar(BigDecimal valor) {
        validarDebito(valor);
        this.saldo = saldo.subtract(valor);
    }

    public void creditar(BigDecimal valor) {
        validarValorPositivo(valor);
        saldo = saldo.add(valor);
    }

    public BigDecimal saldo() {
        return saldo;
    }

    private static void validarValorPositivo(BigDecimal valor) {
        Objects.requireNonNull(valor, "valor é obrigatório");
        if (valor.signum() <= 0) {
            throw new IllegalArgumentException("valor deve ser maior que zero");
        }
    }
}
