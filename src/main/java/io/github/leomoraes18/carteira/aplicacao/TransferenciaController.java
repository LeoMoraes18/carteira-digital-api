package io.github.leomoraes18.carteira.aplicacao;

import io.github.leomoraes18.carteira.dominio.RegraDeNegocioException;
import io.github.leomoraes18.carteira.dominio.Usuario;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public class TransferenciaController {

    private final UsuarioRepository usuarios;
    private final TransferenciaService transferencias;

    public TransferenciaController(UsuarioRepository usuarios, TransferenciaService transferencias) {
        this.usuarios = Objects.requireNonNull(usuarios, "repositório de usuários é obrigatório");
        this.transferencias = Objects.requireNonNull(transferencias, "serviço de transferência é obrigatório");
    }

    public Resposta transferir(Map<String, Object> corpo) {
        BigDecimal valor;
        Long payerId;
        Long payeeId;
        try {
            valor = campoNumericoObrigatorio(corpo, "value");
            payerId = converterParaId(campoNumericoObrigatorio(corpo, "payer"));
            payeeId = converterParaId(campoNumericoObrigatorio(corpo, "payee"));
        } catch (CampoInvalidoException e) {
            return erro(400, e.getMessage());
        }

        try {
            Usuario pagador = buscarUsuario(payerId);
            Usuario recebedor = buscarUsuario(payeeId);

            transferencias.transferir(pagador, recebedor, valor);

            return new Resposta(201, Map.of("mensagem", "transferência concluída"));
        } catch (UsuarioNaoEncontradoException e) {
            return erro(404, e.getMessage());
        } catch (RegraDeNegocioException e) {
            return erro(422, e.getMessage());
        }
    }

    private Usuario buscarUsuario(Long id) {
        return usuarios.buscarPorId(id).orElseThrow(() -> new UsuarioNaoEncontradoException(id));
    }

    private Long converterParaId(BigDecimal numero) {
        try {
            return numero.longValueExact();
        } catch (ArithmeticException e) {
            throw new CampoInvalidoException("id de usuário inválido: " + numero);
        }
    }

    private BigDecimal campoNumericoObrigatorio(Map<String, Object> corpo, String nome) {
        Object valor = corpo.get(nome);
        if (!(valor instanceof BigDecimal numero)) {
            throw new CampoInvalidoException("campo '" + nome + "' é obrigatório e deve ser numérico");
        }
        return numero;
    }

    private Resposta erro(int status, String mensagem) {
        return new Resposta(status, Map.of("erro", mensagem));
    }

    private static final class CampoInvalidoException extends RuntimeException {
        CampoInvalidoException(String mensagem) {
            super(mensagem);
        }
    }
}