package io.github.leomoraes18.carteira.dominio;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UsuarioTest {

    @Test
    void usuarioComumPodeEnviar() {
        Usuario usuario = criarUsuario(TipoUsuario.COMUM, "100.00");

        usuario.enviar(new BigDecimal("40.00"));

        assertEquals(new BigDecimal("60.00"), usuario.carteira().saldo());
    }

    @Test
    void lojistaNaoPodeEnviar() {
        Usuario lojista = criarUsuario(TipoUsuario.LOJISTA, "100.00");

        assertThrows(OperacaoNaoPermitidaException.class,
                () -> lojista.enviar(new BigDecimal("10.00")));

        assertEquals(new BigDecimal("100.00"), lojista.carteira().saldo());
    }

    @Test
    void lojistaPodeReceber() {
        Usuario lojista = criarUsuario(TipoUsuario.LOJISTA, "0.00");

        lojista.receber(new BigDecimal("25.00"));

        assertEquals(new BigDecimal("25.00"), lojista.carteira().saldo());
    }

    @Test
    void naoDeveCriarUsuarioSemTipoOuCarteira() {
        assertThrows(NullPointerException.class,
                () -> new Usuario(1L, "Ana Souza", "12345678900", "ana@email.com", null,
                        new Carteira(BigDecimal.ZERO)));
        assertThrows(NullPointerException.class,
                () -> new Usuario(1L, "Ana Souza", "12345678900", "ana@email.com",
                        TipoUsuario.COMUM, null));

    }

    private Usuario criarUsuario(TipoUsuario tipo, String saldoInicial) {
        return new Usuario(1L, "Ana Souza", "12345678900", "ana@email.com",
                tipo, new Carteira(new BigDecimal(saldoInicial)));
    }
}
