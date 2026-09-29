package io.github.leomoraes18.carteira.infra.http;

import com.sun.net.httpserver.HttpServer;
import io.github.leomoraes18.carteira.dominio.Carteira;
import io.github.leomoraes18.carteira.dominio.TipoUsuario;
import io.github.leomoraes18.carteira.dominio.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutorizadorHttpTest {

    private HttpServer servidorFake;

    @AfterEach
    void pararServidor() {
        if (servidorFake != null) {
            servidorFake.stop(0);
        }
    }

    @Test
    void deveAutorizarQuandoServicoRespondeAutorizacaoTrue() throws IOException {
        String url = subirServidorFake(200, """
                {"status": "success", "data": {"authorization": true}}
                """);
        AutorizadorHttp autorizador = new AutorizadorHttp(url);

        assertTrue(autorizador.autorizar(usuario(1L), usuario(2L), new BigDecimal("30.00")));
    }

    @Test
    void naoDeveAutorizarQuandoServicoRespondeAutorizacaoFalse() throws IOException {
        String url = subirServidorFake(200, """
                {"status": "fail", "data": {"authorization": false}}
                """);
        AutorizadorHttp autorizador = new AutorizadorHttp(url);

        assertFalse(autorizador.autorizar(usuario(1L), usuario(2L), new BigDecimal("30.00")));
    }

    @Test
    void naoDeveAutorizarQuandoServicoRespondeErro() throws IOException {
        String url = subirServidorFake(500, "erro interno");
        AutorizadorHttp autorizador = new AutorizadorHttp(url);

        assertFalse(autorizador.autorizar(usuario(1L), usuario(2L), new BigDecimal("30.00")));
    }

    @Test
    void naoDeveAutorizarQuandoServicoEstaIndisponivel() {
        AutorizadorHttp autorizador = new AutorizadorHttp("http://localhost:1");

        assertFalse(autorizador.autorizar(usuario(1L), usuario(2L), new BigDecimal("30.00")));
    }

    private String subirServidorFake(int status, String corpo) throws IOException {
        servidorFake = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidorFake.createContext("/authorize", exchange -> {
            byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (var saida = exchange.getResponseBody()) {
                saida.write(bytes);
            }
        });
        servidorFake.start();
        return "http://localhost:" + servidorFake.getAddress().getPort() + "/authorize";
    }

    private Usuario usuario(Long id) {
        return new Usuario(id, "Usuario " + id, "0000000000" + id, "usuario" + id + "@email.com",
                TipoUsuario.COMUM, new Carteira(BigDecimal.ZERO));
    }
}