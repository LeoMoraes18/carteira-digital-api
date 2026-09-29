package io.github.leomoraes18.carteira.infra.http;

import com.sun.net.httpserver.HttpServer;
import io.github.leomoraes18.carteira.aplicacao.Notificacao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificadorHttpTest {

    private HttpServer servidorFake;

    @AfterEach
    void pararServidor() {
        if (servidorFake != null) {
            servidorFake.stop(0);
        }
    }

    @Test
    void deveEnviarNotificacaoComMetodoPostQuandoServicoResponde2xx() throws IOException {
        AtomicReference<String> metodoRecebido = new AtomicReference<>();
        String url = subirServidorFake(200, exchange -> metodoRecebido.set(exchange.getRequestMethod()));
        NotificadorHttp notificador = new NotificadorHttp(url);

        Notificacao notificacao = new Notificacao(2L, "recebedor@email.com", new BigDecimal("30.00"));

        assertDoesNotThrow(() -> notificador.enviar(notificacao));
        assertEquals("POST", metodoRecebido.get());
    }

    @Test
    void deveLancarExcecaoQuandoServicoRespondeErro() throws IOException {
        String url = subirServidorFake(500, exchange -> { });
        NotificadorHttp notificador = new NotificadorHttp(url);

        Notificacao notificacao = new Notificacao(2L, "recebedor@email.com", new BigDecimal("30.00"));

        assertThrows(NotificacaoFalhouException.class, () -> notificador.enviar(notificacao));
    }

    @Test
    void deveLancarExcecaoQuandoServicoEstaIndisponivel() {
        NotificadorHttp notificador = new NotificadorHttp("http://localhost:1");

        Notificacao notificacao = new Notificacao(2L, "recebedor@email.com", new BigDecimal("30.00"));

        assertThrows(NotificacaoFalhouException.class, () -> notificador.enviar(notificacao));
    }

    private String subirServidorFake(int status, com.sun.net.httpserver.HttpHandler comportamento) throws IOException {
        servidorFake = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidorFake.createContext("/notify", exchange -> {
            comportamento.handle(exchange);
            byte[] bytes = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (var saida = exchange.getResponseBody()) {
                saida.write(bytes);
            }
        });
        servidorFake.start();
        return "http://localhost:" + servidorFake.getAddress().getPort() + "/notify";
    }
}