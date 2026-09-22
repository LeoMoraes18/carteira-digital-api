package io.github.leomoraes18.carteira.aplicacao;

import io.github.leomoraes18.carteira.dominio.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TransferenciaServiceTest {

    private static final AutorizadorTransferencia SEMPRE_AUTORIZA = (pagador, recebedor, valor) -> true;
    private static final AutorizadorTransferencia NUNCA_AUTORIZA = (pagador, recebedor, valor) -> false;

    private final List<Notificacao> agendadas = new ArrayList<>();

    @Test
    void deveTransferirQuandoAutorizado() {
        Usuario pagador = criarUsuario(1L, TipoUsuario.COMUM, "100.00");
        Usuario recebedor = criarUsuario(2L, TipoUsuario.LOJISTA, "0.00");
        TransferenciaService service = criarService(SEMPRE_AUTORIZA);

        service.transferir(pagador, recebedor, new BigDecimal("30.00"));

        assertEquals(new BigDecimal("70.00"), pagador.carteira().saldo());
        assertEquals(new BigDecimal("30.00"), recebedor.carteira().saldo());
    }

    @Test
    void naoDeveTransferirQuandoNaoAutorizado() {
        Usuario pagador = criarUsuario(1L, TipoUsuario.COMUM, "100.00");
        Usuario recebedor = criarUsuario(2L, TipoUsuario.LOJISTA, "0.00");
        TransferenciaService service = criarService(NUNCA_AUTORIZA);

        assertThrows(TransferenciaNaoAutorizadaException.class,
                () -> service.transferir(pagador, recebedor, new BigDecimal("30.00")));

        assertEquals(new BigDecimal("100.00"), pagador.carteira().saldo());
        assertEquals(new BigDecimal("0.00"), recebedor.carteira().saldo());
    }

    @Test
    void naoDeveConsultarAutorizadorQuandoSaldoInsuficiente() {
        AtomicInteger chamadas = new AtomicInteger();
        AutorizadorTransferencia autorizador = (pagador, recebedor, valor) -> {
            chamadas.incrementAndGet();
            return true;
        };
        Usuario pagador = criarUsuario(1L, TipoUsuario.COMUM, "10.00");
        Usuario recebedor = criarUsuario(2L, TipoUsuario.COMUM, "0.00");
        TransferenciaService service = criarService(autorizador);

        assertThrows(SaldoInsuficienteException.class,
                () -> service.transferir(pagador, recebedor, new BigDecimal("50.00")));

        assertEquals(0, chamadas.get());
    }

    @Test
    void lojistaNaoPodeTransferir() {
        Usuario lojista = criarUsuario(1L, TipoUsuario.LOJISTA, "100.00");
        Usuario recebedor = criarUsuario(2L, TipoUsuario.COMUM, "0.00");
        TransferenciaService service = criarService(SEMPRE_AUTORIZA);

        assertThrows(OperacaoNaoPermitidaException.class,
                () -> service.transferir(lojista, recebedor, new BigDecimal("10.00")));
    }

    @Test
    void naoDeveTransferirParaSiMesmo() {
        Usuario usuario = criarUsuario(1L, TipoUsuario.COMUM, "100.00");
        TransferenciaService service = criarService(SEMPRE_AUTORIZA);

        assertThrows(OperacaoNaoPermitidaException.class,
                () -> service.transferir(usuario, usuario, new BigDecimal("10.00")));

        assertEquals(new BigDecimal("100.00"), usuario.carteira().saldo());
    }

    @Test
    void deveAgendarNotificacaoParaRecebedorAposTransferir() {
        Usuario pagador = criarUsuario(1L, TipoUsuario.COMUM, "100.00");
        Usuario recebedor = criarUsuario(2L, TipoUsuario.COMUM, "0.00");

        criarService(SEMPRE_AUTORIZA).transferir(pagador, recebedor, new BigDecimal("30.00"));

        assertEquals(List.of(new Notificacao(2L, "usuario2@email.com", new BigDecimal("30.00"))), agendadas);
    }

    @Test
    void naoDeveAgendarNotificacaoQuandoTransferenciaFalha() {
        Usuario pagador = criarUsuario(1L, TipoUsuario.COMUM, "100.00");
        Usuario recebedor = criarUsuario(2L, TipoUsuario.COMUM, "0.00");

        assertThrows(TransferenciaNaoAutorizadaException.class,
                () -> criarService(NUNCA_AUTORIZA).transferir(pagador, recebedor, new BigDecimal("30.00")));

        assertTrue(agendadas.isEmpty());
    }

    private Usuario criarUsuario(Long id, TipoUsuario tipo, String saldo) {
        return new Usuario(id, "Usuario " + id, "0000000000" + id, "usuario" + id + "@email.com",
                tipo, new Carteira(new BigDecimal(saldo)));
    }

    private TransferenciaService criarService(AutorizadorTransferencia autorizador) {
        return new TransferenciaService(autorizador, agendadas::add);
    }
}
