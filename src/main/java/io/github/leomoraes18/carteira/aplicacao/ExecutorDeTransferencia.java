package io.github.leomoraes18.carteira.aplicacao;

import java.util.Map;

@FunctionalInterface
public interface ExecutorDeTransferencia {

    Resposta transferir(Map<String, Object> corpo);
}
