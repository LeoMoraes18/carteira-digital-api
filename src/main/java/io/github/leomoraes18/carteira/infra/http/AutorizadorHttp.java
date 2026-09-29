package io.github.leomoraes18.carteira.infra.http;

import io.github.leomoraes18.carteira.aplicacao.AutorizadorTransferencia;
import io.github.leomoraes18.carteira.dominio.Usuario;
import io.github.leomoraes18.carteira.infra.web.Json;
import io.github.leomoraes18.carteira.infra.web.JsonParseException;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class AutorizadorHttp implements AutorizadorTransferencia {

    private final String url;
    private final HttpClient cliente;

    public AutorizadorHttp(String url) {
        this.url = Objects.requireNonNull(url, "url do autorizador é obrigatória");
        this.cliente = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Override
    public boolean autorizar(Usuario pagador, Usuario recebedor, BigDecimal valor) {
        try {
            HttpRequest requisicao = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());

            if (resposta.statusCode() != 200) {
                return false;
            }
            return extrairAutorizacao(resposta.body());
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean extrairAutorizacao(String corpo) {
        try {
            Object json = Json.parse(corpo);
            Map<String, Object> raiz = (Map<String, Object>) json;
            Map<String, Object> data = (Map<String, Object>) raiz.get("data");
            if (data == null) {
                return false;
            }
            return Boolean.TRUE.equals(data.get("authorization"));
        } catch (JsonParseException | ClassCastException e) {
            return false;
        }
    }
}