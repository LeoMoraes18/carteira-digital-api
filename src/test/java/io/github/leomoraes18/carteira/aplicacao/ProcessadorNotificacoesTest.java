package io.github.leomoraes18.carteira.aplicacao;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessadorNotificacoesTest {

    private static final Notificacao NOTIFICACAO =
            new Notificacao(2L, "usuario2@email.com", new BigDecimal("30.00"));

    @Test
    void deveEnviarNotificacaoPendente() {
        FilaNotificacoesEmMemoria fila = new FilaNotificacoesEmMemoria();
        fila.agendar(NOTIFICACAO);
        List<Notificacao> enviadas = new java.util.ArrayList<>();
        ProcessadorNotificacoes processador =
                new ProcessadorNotificacoes(fila, enviadas::add);

        processador.processarPendentes();

        assertEquals(List.of(NOTIFICACAO), enviadas);
        assertTrue(fila.pendentes().isEmpty());
    }

    @Test
    void deveManterNaFilaQuandoEnvioFalha() {
        FilaNotificacoesEmMemoria fila = new FilaNotificacoesEmMemoria();
        fila.agendar(NOTIFICACAO);
        Notificador notificadorInstavel = notificacao -> {
            throw new RuntimeException("serviço de notificação fora do ar");
        };
        ProcessadorNotificacoes processador =
                new ProcessadorNotificacoes(fila, notificadorInstavel);

        processador.processarPendentes();

        assertEquals(List.of(NOTIFICACAO), fila.pendentes());
    }

    @Test
    void deveContinuarProcessandoAsDemaisQuandoUmaFalha() {
        FilaNotificacoesEmMemoria fila = new FilaNotificacoesEmMemoria();

        Notificacao notificacaoRuim = new Notificacao(1L, "ruim@email.com", BigDecimal.TEN);
        Notificacao notificacaoBoa = new Notificacao(2L, "boa@email.com", BigDecimal.ONE);

        fila.agendar(notificacaoRuim);
        fila.agendar(notificacaoBoa);

        AtomicInteger tentativas = new AtomicInteger();

        List<Notificacao> enviadas = new java.util.ArrayList<>();

        Notificador notificador = notificacao -> {
            tentativas.incrementAndGet();
            if (notificacao.equals(notificacaoRuim)) {
                throw new RuntimeException("falha simulada");
            }
            enviadas.add(notificacao);
        };

        ProcessadorNotificacoes processador = new ProcessadorNotificacoes(fila, notificador);

        processador.processarPendentes();

        assertEquals(2, tentativas.get());
        assertEquals(List.of(notificacaoBoa), enviadas);
        assertEquals(List.of(notificacaoRuim), fila.pendentes());
    }
}
