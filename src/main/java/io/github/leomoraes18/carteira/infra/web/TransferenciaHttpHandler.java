package io.github.leomoraes18.carteira.infra.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.leomoraes18.carteira.aplicacao.ExecutorDeTransferencia;
import io.github.leomoraes18.carteira.aplicacao.Resposta;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class TransferenciaHttpHandler implements HttpHandler {

    private final ExecutorDeTransferencia executor;

    public TransferenciaHttpHandler(ExecutorDeTransferencia executor) {
        this.executor = Objects.requireNonNull(executor, "executor é obrigatório");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            processar(exchange);
        } catch (RuntimeException e) {
            e.printStackTrace();
            escreverResposta(exchange, new Resposta(500,
                    Map.of("erro", "erro interno: " + e)));
        }
    }

    private void processar(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            escreverResposta(exchange, new Resposta(405, Map.of("erro", "método não permitido")));
            return;
        }

        Resposta resposta;
        try {
            String corpoBruto = lerCorpo(exchange);
            Object json = Json.parse(corpoBruto);
            @SuppressWarnings("unchecked")
                    Map<String, Object> corpo = (Map<String, Object>) json;
            resposta = executor.transferir(corpo);
        } catch (JsonParseException | ClassCastException e) {
            resposta = new Resposta(400, Map.of("erro", "corpo da requisição não é um Json válido"));
        }

        escreverResposta(exchange, resposta);
    }

    private String lerCorpo(HttpExchange exchange) throws IOException {
        try (InputStream entrada = exchange.getRequestBody()) {
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void escreverResposta(HttpExchange exchange, Resposta resposta) throws IOException {
        String json = Json.write(resposta.corpo());
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(resposta.status(), bytes.length);

        try (OutputStream saida = exchange.getResponseBody()) {
            saida.write(bytes);
        }
    }

}
