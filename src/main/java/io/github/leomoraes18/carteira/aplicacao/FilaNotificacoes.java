package io.github.leomoraes18.carteira.aplicacao;

import java.util.List;

public interface FilaNotificacoes extends AgendadorNotificacoes {

    List<Notificacao> pendentes();

    void remover(Notificacao notificacao);
}
