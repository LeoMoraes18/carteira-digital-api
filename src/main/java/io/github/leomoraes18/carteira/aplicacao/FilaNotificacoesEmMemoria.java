package io.github.leomoraes18.carteira.aplicacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilaNotificacoesEmMemoria implements FilaNotificacoes {

    private final List<Notificacao> pendentes = new ArrayList<>();

    @Override
    public void agendar(Notificacao notificacao) {
        pendentes.add(notificacao);
    }

    @Override
    public List<Notificacao> pendentes() {
        return Collections.unmodifiableList(pendentes);
    }

    @Override
    public void remover(Notificacao notificacao) {
        pendentes.remove(notificacao);
    }
}
