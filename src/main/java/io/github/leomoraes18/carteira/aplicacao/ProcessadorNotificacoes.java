package io.github.leomoraes18.carteira.aplicacao;

import java.util.List;
import java.util.Objects;

public class ProcessadorNotificacoes {

    private final FilaNotificacoes fila;
    private final Notificador notificador;

    public ProcessadorNotificacoes(FilaNotificacoes fila, Notificador notificador) {
        this.fila = Objects.requireNonNull(fila, "fila é obrigatório");
        this.notificador = Objects.requireNonNull(notificador, "notificador é obrigatório");
    }

    public void processarPendentes() {
        List<Notificacao> pendentes = List.copyOf(fila.pendentes());
        for (Notificacao notificacao : pendentes) {
            try {
                notificador.enviar(notificacao);
                fila.remover(notificacao);
            } catch (RuntimeException e) {

            }
        }
    }
}
