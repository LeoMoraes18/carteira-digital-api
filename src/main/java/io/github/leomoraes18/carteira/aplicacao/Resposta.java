package io.github.leomoraes18.carteira.aplicacao;

import java.util.Map;

public record Resposta(int status, Map<String, Object> corpo) {
}
