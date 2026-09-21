package io.github.leomoraes18.carteira.aplicacao;

import java.math.BigDecimal;

public record Notificacao(Long destinatarioId, String email, BigDecimal valor) {
}
