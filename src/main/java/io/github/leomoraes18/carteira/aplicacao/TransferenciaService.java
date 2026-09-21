package io.github.leomoraes18.carteira.aplicacao;

import io.github.leomoraes18.carteira.dominio.OperacaoNaoPermitidaException;
import io.github.leomoraes18.carteira.dominio.TransferenciaNaoAutorizadaException;
import io.github.leomoraes18.carteira.dominio.Usuario;

import java.math.BigDecimal;
import java.util.Objects;

public class TransferenciaService {

    private final AutorizadorTransferencia autorizador;

    public TransferenciaService(AutorizadorTransferencia autorizador) {
        this.autorizador = Objects.requireNonNull(autorizador, "autorizador é obrigatório");
    }


    public void transferir(Usuario pagador, Usuario recebedor, BigDecimal valor) {
        Objects.requireNonNull(pagador, "pagador é obrigatório");
        Objects.requireNonNull(recebedor, "recebedor é obrigatório");

        if (Objects.equals(pagador.id(), recebedor.id())) {
            throw new OperacaoNaoPermitidaException("não é possivel transferir para si mesmo");
        }

        pagador.validarEnvio(valor);

        if (!autorizador.autorizar(pagador, recebedor, valor)) {
            throw new TransferenciaNaoAutorizadaException();
        }

        pagador.enviar(valor);
        recebedor.receber(valor);
    }
}
