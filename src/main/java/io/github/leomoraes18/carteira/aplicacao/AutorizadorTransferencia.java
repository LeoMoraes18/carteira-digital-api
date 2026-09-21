package io.github.leomoraes18.carteira.aplicacao;

import io.github.leomoraes18.carteira.dominio.Usuario;

import java.math.BigDecimal;

@FunctionalInterface
public interface AutorizadorTransferencia {

    boolean autorizar(Usuario pagador, Usuario recebedor, BigDecimal valor);
}
