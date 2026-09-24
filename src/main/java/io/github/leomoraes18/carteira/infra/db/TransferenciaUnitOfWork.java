package io.github.leomoraes18.carteira.infra.db;

import io.github.leomoraes18.carteira.aplicacao.AutorizadorTransferencia;
import io.github.leomoraes18.carteira.aplicacao.ExecutorDeTransferencia;
import io.github.leomoraes18.carteira.aplicacao.Resposta;
import io.github.leomoraes18.carteira.aplicacao.TransferenciaController;
import io.github.leomoraes18.carteira.aplicacao.TransferenciaService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

public class TransferenciaUnitOfWork implements ExecutorDeTransferencia {

    private final ConexaoFactory conexoes;
    private final AutorizadorTransferencia autorizador;

    public TransferenciaUnitOfWork(ConexaoFactory conexoes, AutorizadorTransferencia autorizador) {
        this.conexoes = Objects.requireNonNull(conexoes, "fábrica de conexões é obrigatória");
        this.autorizador = Objects.requireNonNull(autorizador, "autorizador é obrigatório");
    }

    @Override
    public Resposta transferir(Map<String, Object> corpo) {
        try (Connection conexao = conexoes.criar()) {
            conexao.setAutoCommit(false);
            try {
                Resposta resposta = executarDentroDaTransacao(conexao, corpo);
                if (resposta.status() >= 200 && resposta.status() < 300) {
                    conexao.commit();
                } else {
                    conexao.rollback();
                }
                return resposta;
            } catch (RuntimeException e) {
                conexao.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ConexaoException("erro ao executar transferência", e);
        }
    }

    private Resposta executarDentroDaTransacao(Connection conexao, Map<String, Object> corpo) {
        UsuarioRepositoryTransacional usuarios = new UsuarioRepositoryTransacional(conexao);
        AgendadorNotificacoesJdbc notificacoes = new AgendadorNotificacoesJdbc(conexao);
        TransferenciaService service = new TransferenciaService(autorizador, notificacoes);
        TransferenciaController controller = new TransferenciaController(usuarios, service);

        return controller.transferir(corpo);
    }
}