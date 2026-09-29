package io.github.leomoraes18.carteira.infra.http;

import io.github.leomoraes18.carteira.aplicacao.Notificacao;
import io.github.leomoraes18.carteira.aplicacao.Notificador;
import io.github.leomoraes18.carteira.infra.web.Json;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class NotificadorHttp implements Notificador {

    private final String url;
    private final HttpClient cliente;

    public NotificadorHttp(String url) {
        this.url = Objects.requireNonNull(url, "url do notificador é obrigatória");
        this.cliente = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Override
    public void enviar(Notificacao notificacao) {
        try {
            String corpo = Json.write(Map.of(
                    "destinatarioId", BigDecimal.valueOf(notificacao.destinatarioId()),
                    "email", notificacao.email(),
                    "valor", notificacao.valor()));

            HttpRequest requisicao = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(corpo))
                    .build();

            HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());

            if (resposta.statusCode() < 200 || resposta.statusCode() >= 300) {
                throw new NotificacaoFalhouException(
                        "serviço de notificação respondeu status " + resposta.statusCode());
            }
        } catch (NotificacaoFalhouException e) {
            throw e;
        } catch (Exception e) {
            throw new NotificacaoFalhouException("erro ao enviar notificação", e);
        }
    }
}