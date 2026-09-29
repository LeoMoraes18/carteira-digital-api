package io.github.leomoraes18.carteira.infra.db;

import io.github.leomoraes18.carteira.aplicacao.FilaNotificacoes;
import io.github.leomoraes18.carteira.aplicacao.Notificacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FilaNotificacoesJdbc implements FilaNotificacoes {

    private static final String SQL_INSERIR = """
            INSERT INTO notificacoes_pendentes (destinatario_id, email, valor)
            VALUES (?, ?, ?)
            """;

    private static final String SQL_LISTAR_PENDENTES = """
            SELECT destinatario_id, email, valor
            FROM notificacoes_pendentes
            ORDER BY criada_em
            """;

    private static final String SQL_REMOVER_UMA = """
            DELETE FROM notificacoes_pendentes
            WHERE ctid = (
                SELECT ctid FROM notificacoes_pendentes
                WHERE destinatario_id = ? AND email = ? AND valor = ?
                LIMIT 1
            )
            """;

    private final ConexaoFactory conexoes;

    public FilaNotificacoesJdbc(ConexaoFactory conexoes) {
        this.conexoes = Objects.requireNonNull(conexoes, "fábrica de conexões é obrigatória");
    }

    @Override
    public void agendar(Notificacao notificacao) {
        try (Connection conexao = conexoes.criar();
             PreparedStatement statement = conexao.prepareStatement(SQL_INSERIR)) {
            preencher(statement, notificacao);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ConexaoException("erro ao agendar notificação para: " + notificacao.destinatarioId(), e);
        }
    }

    @Override
    public List<Notificacao> pendentes() {
        try (Connection conexao = conexoes.criar();
             PreparedStatement statement = conexao.prepareStatement(SQL_LISTAR_PENDENTES);
             ResultSet resultado = statement.executeQuery()) {
            List<Notificacao> pendentes = new ArrayList<>();
            while (resultado.next()) {
                pendentes.add(new Notificacao(
                        resultado.getLong("destinatario_id"),
                        resultado.getString("email"),
                        resultado.getBigDecimal("valor")));
            }
            return pendentes;
        } catch (SQLException e) {
            throw new ConexaoException("erro ao listar notificações pendentes", e);
        }
    }

    @Override
    public void remover(Notificacao notificacao) {
        try (Connection conexao = conexoes.criar();
             PreparedStatement statement = conexao.prepareStatement(SQL_REMOVER_UMA)) {
            preencher(statement, notificacao);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ConexaoException("erro ao remover notificação de: " + notificacao.destinatarioId(), e);
        }
    }

    private void preencher(PreparedStatement statement, Notificacao notificacao) throws SQLException {
        statement.setLong(1, notificacao.destinatarioId());
        statement.setString(2, notificacao.email());
        statement.setBigDecimal(3, notificacao.valor());
    }
}