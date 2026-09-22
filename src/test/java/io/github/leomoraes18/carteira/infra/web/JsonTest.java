package io.github.leomoraes18.carteira.infra.web;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonTest {

    @Test
    void deveLerObjetoSimples() {
        Object resultado = Json.parse("""
                {"value": 100.00, "payer": 1, "payee": 2}
                """);

        Map<String, Object> objeto = (Map<String, Object>) resultado;
        assertEquals(new BigDecimal("100.00"), objeto.get("value"));
        assertEquals(new BigDecimal("1"), objeto.get("payer"));
        assertEquals(new BigDecimal("2"), objeto.get("payee"));
    }

    @Test
    void deveLerString() {
        Map<String, Object> objeto = (Map<String, Object>) Json.parse("""
                {"nome": "Ana Souza"}
                """);

        assertEquals("Ana Souza", objeto.get("nome"));
    }

    @Test
    void deveLerNuloEBooleano() {
        Map<String, Object> objeto = (Map<String, Object>) Json.parse("""
                {"ativo": true, "removido": null}
                """);

        assertEquals(Boolean.TRUE, objeto.get("ativo"));
        assertNull(objeto.get("removido"));
    }

    @Test
    void deveLerObjetoAninhadoEArray() {
        Map<String, Object> objeto = (Map<String, Object>) Json.parse("""
                {"endereco": {"cidade": "Sinop"}, "tags": ["a", "b"]}
                """);

        Map<String, Object> endereco = (Map<String, Object>) objeto.get("endereco");
        assertEquals("Sinop", endereco.get("cidade"));
        assertEquals(List.of("a", "b"), objeto.get("tags"));
    }

    @Test
    void deveFalharComJsonInvalido() {
        assertThrows(JsonParseException.class, () -> Json.parse("{value: 1}"));
    }

    @Test
    void deveEscreverObjetoSimples() {
        String json = Json.write(Map.of("erro", "saldo insuficiente"));

        assertEquals("{\"erro\":\"saldo insuficiente\"}", json);
    }

    @Test
    void deveEscreverNumeroEBooleano() {
        Map<String, Object> objeto = new java.util.LinkedHashMap<>();
        objeto.put("valor", new BigDecimal("30.00"));
        objeto.put("ok", true);

        assertEquals("{\"valor\":30.00,\"ok\":true}", Json.write(objeto));
    }
}