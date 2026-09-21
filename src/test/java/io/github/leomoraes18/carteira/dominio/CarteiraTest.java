package io.github.leomoraes18.carteira.dominio;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CarteiraTest {

    @Test
    void deveDebitarQuandoHaSaldoSuficiente() {
        Carteira carteira = new Carteira(new BigDecimal("100.00"));

        carteira.debitar(new BigDecimal("30.00"));

        assertEquals(new BigDecimal("70.00"), carteira.saldo());
    }

    @Test
    void naoDeveDebitarValorMaiorQueOSaldo() {
        Carteira carteira = new Carteira(new BigDecimal("50.00"));

        assertThrows(SaldoInsuficienteException.class,
                () -> carteira.debitar(new BigDecimal("50.01")));

        assertEquals(new BigDecimal("50.00"), carteira.saldo());
    }

    @Test
    void deveCreditarValor() {
        Carteira carteira = new Carteira(new BigDecimal("10.00"));

        carteira.creditar(new BigDecimal("5.50"));

        assertEquals(new BigDecimal("15.50"), carteira.saldo());
    }

    @Test
    void naoDeveAceitarValorZeroOuNegativo() {
        Carteira carteira = new Carteira(new BigDecimal("10.00"));

        assertThrows(IllegalArgumentException.class,
                () -> carteira.debitar(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> carteira.creditar(new BigDecimal("-1.00")));
    }
}