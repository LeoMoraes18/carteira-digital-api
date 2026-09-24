package io.github.leomoraes18.carteira.infra.db;

import io.github.leomoraes18.carteira.aplicacao.AgendadorNotificacoes;
import io.github.leomoraes18.carteira.aplicacao.Notificacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class AgendadorNotificacoesJdbc implements AgendadorNotificacoes {

    private static final String SQL_INSERIR = """
            INSERT INTO notificacoes_pendentes (destinatario_id, email, valor)
            VALUES (?, ?, ?)
            """;

    private final Connection conexao;

    public AgendadorNotificacoesJdbc(Connection conexao) {
        this.conexao = Objects.requireNonNull(conexao, "conexão é obrigatória");
    }

    @Override
    public void agendar(Notificacao notificacao) {
        try (PreparedStatement statement = conexao.prepareStatement(SQL_INSERIR)) {
            statement.setLong(1, notificacao.destinatarioId());
            statement.setString(2, notificacao.email());
            statement.setBigDecimal(3, notificacao.valor());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ConexaoException("erro ao agendar notificação para: " + notificacao.destinatarioId(), e);
        }
    }
}