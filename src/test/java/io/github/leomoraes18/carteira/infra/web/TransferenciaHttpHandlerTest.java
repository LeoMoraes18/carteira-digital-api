package io.github.leomoraes18.carteira.infra.web;

import com.sun.net.httpserver.HttpServer;
import io.github.leomoraes18.carteira.aplicacao.TransferenciaController;
import io.github.leomoraes18.carteira.aplicacao.TransferenciaService;
import io.github.leomoraes18.carteira.aplicacao.UsuarioRepositoryEmMemoria;
import io.github.leomoraes18.carteira.dominio.Carteira;
import io.github.leomoraes18.carteira.dominio.TipoUsuario;
import io.github.leomoraes18.carteira.dominio.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferenciaHttpHandlerTest {

    private HttpServer servidor;
    private HttpClient cliente;
    private String baseUrl;

    @BeforeEach
    void subirServidor() throws IOException {
        UsuarioRepositoryEmMemoria repositorio = new UsuarioRepositoryEmMemoria();
        repositorio.salvar(new Usuario(1L, "Pagador", "11111111111", "pagador@email.com",
                TipoUsuario.COMUM, new Carteira(new BigDecimal("100.00"))));
        repositorio.salvar(new Usuario(2L, "Recebedor", "22222222222", "recebedor@email.com",
                TipoUsuario.LOJISTA, new Carteira(new BigDecimal("0.00"))));

        TransferenciaService service = new TransferenciaService(
                (pagador, recebedor, valor) -> true,
                notificacao -> { });
        TransferenciaController controller = new TransferenciaController(repositorio, service);

        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/transfer", new TransferenciaHttpHandler(controller));
        servidor.start();

        baseUrl = "http://localhost:" + servidor.getAddress().getPort();
        cliente = HttpClient.newHttpClient();
    }

    @AfterEach
    void pararServidor() {
        servidor.stop(0);
    }

    @Test
    void deveRetornar201ERegistrarNoContentTypeCorreto() throws Exception {
        String corpo = """
                {"value": 30.00, "payer": 1, "payee": 2}
                """;
        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/transfer"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .build();

        HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, resposta.statusCode());
        assertEquals("application/json", resposta.headers().firstValue("Content-Type").orElseThrow());
        Map<?, ?> corpoResposta = (Map<?, ?>) Json.parse(resposta.body());
        assertTrue(corpoResposta.containsKey("mensagem"));
    }

    @Test
    void deveRetornar404QuandoUsuarioNaoExiste() throws Exception {
        String corpo = """
                {"value": 30.00, "payer": 1, "payee": 999}
                """;
        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/transfer"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .build();

        HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, resposta.statusCode());
    }

    @Test
    void deveRetornar400QuandoJsonInvalido() throws Exception {
        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/transfer"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{invalido"))
                .build();

        HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, resposta.statusCode());
    }

    @Test
    void deveRetornar405QuandoMetodoNaoEPost() throws Exception {
        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/transfer"))
                .GET()
                .build();

        HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, resposta.statusCode());
    }
}