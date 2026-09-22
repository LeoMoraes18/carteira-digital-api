package io.github.leomoraes18.carteira.aplicacao;

import io.github.leomoraes18.carteira.dominio.Carteira;
import io.github.leomoraes18.carteira.dominio.TipoUsuario;
import io.github.leomoraes18.carteira.dominio.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransferenciaControllerTest {

    private UsuarioRepositoryEmMemoria repositorio;
    private TransferenciaController controller;

    @BeforeEach
    void configurar() {
        repositorio = new UsuarioRepositoryEmMemoria();
        repositorio.salvar(new Usuario(1L, "Pagador", "11111111111", "pagador@email.com",
                TipoUsuario.COMUM, new Carteira(new BigDecimal("100.00"))));
        repositorio.salvar(new Usuario(2L, "Recebedor", "22222222222", "recebedor@email.com",
                TipoUsuario.LOJISTA, new Carteira(new BigDecimal("0.00"))));

        TransferenciaService service = new TransferenciaService(
                ((pagador, recebedor, valor) -> true),
                notificacao -> {});
        controller = new TransferenciaController(repositorio, service);
    }

    @Test
    void deveRetornar201QuandoTransferenciaOcorreComSucesso() {
        Resposta resposta = controller.transferir(Map.of(
                "value", new BigDecimal("30.00"),
                "payer", new BigDecimal("1"),
                "payee", new BigDecimal("2")));

        assertEquals(201, resposta.status());
    }

    @Test
    void deveRetornar404QuandoUsuarioNaoExiste() {
        Resposta resposta = controller.transferir(Map.of(
                "value", new BigDecimal("30.00"),
                "payer", new BigDecimal("1"),
                "payee", new BigDecimal("999")));

        assertEquals(404, resposta.status());
    }

    @Test
    void deveRetornar422QuandoSaldoInsuficiente() {
        Resposta resposta = controller.transferir(Map.of(
                "value", new BigDecimal("500.00"),
                "payer", new BigDecimal("1"),
                "payee", new BigDecimal("2")));

        assertEquals(422, resposta.status());
    }

    @Test
    void deveRetornar400QuandoCampoObrigatorioAusente() {
        Resposta resposta = controller.transferir(Map.of(
                "payer", new BigDecimal("1"),
                "payee", new BigDecimal("2")));

        assertEquals(400, resposta.status());
    }
}
