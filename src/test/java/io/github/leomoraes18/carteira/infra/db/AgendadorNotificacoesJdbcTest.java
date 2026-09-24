package io.github.leomoraes18.carteira.infra.db;

import io.github.leomoraes18.carteira.aplicacao.Notificacao;
import io.github.leomoraes18.carteira.dominio.TipoUsuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgendadorNotificacoesJdbcTest {

    private static final ConexaoFactory CONEXAO = new ConexaoFactory(
            "jdbc:postgresql://localhost:5433/carteira_digital", "carteira", "carteira");

    private Long idUsuario;

    @BeforeEach
    void configurar() throws SQLException {
        idUsuario = inserirUsuario("Recebedor Teste", "44444444444",
                "recebedor.teste@email.com", TipoUsuario.LOJISTA, BigDecimal.ZERO);
    }

    @AfterEach
    void limpar() throws SQLException {
        try (Connection conexao = CONEXAO.criar()) {
            executar(conexao, "DELETE FROM notificacoes_pendentes WHERE destinatario_id = ?", idUsuario);
            executar(conexao, "DELETE FROM usuarios WHERE id = ?", idUsuario);
        }
    }

    @Test
    void deveAgendarNotificacaoDentroDaTransacao() throws SQLException {
        Notificacao notificacao = new Notificacao(idUsuario, "recebedor.teste@email.com",
                new BigDecimal("30.00"));

        try (Connection conexao = CONEXAO.criar()) {
            conexao.setAutoCommit(false);
            AgendadorNotificacoesJdbc agendador = new AgendadorNotificacoesJdbc(conexao);

            agendador.agendar(notificacao);
            conexao.commit();
        }

        assertEquals(1, contarNotificacoesPendentes());
    }

    @Test
    void naoDeveGravarNotificacaoQuandoTransacaoERevertida() throws SQLException {
        Notificacao notificacao = new Notificacao(idUsuario, "recebedor.teste@email.com",
                new BigDecimal("30.00"));

        try (Connection conexao = CONEXAO.criar()) {
            conexao.setAutoCommit(false);
            AgendadorNotificacoesJdbc agendador = new AgendadorNotificacoesJdbc(conexao);

            agendador.agendar(notificacao);
            conexao.rollback();
        }

        assertEquals(0, contarNotificacoesPendentes());
    }

    private int contarNotificacoesPendentes() throws SQLException {
        try (Connection conexao = CONEXAO.criar();
             PreparedStatement statement = conexao.prepareStatement(
                     "SELECT COUNT(*) FROM notificacoes_pendentes WHERE destinatario_id = ?")) {
            statement.setLong(1, idUsuario);
            try (ResultSet resultado = statement.executeQuery()) {
                assertTrue(resultado.next());
                return resultado.getInt(1);
            }
        }
    }

    private void executar(Connection conexao, String sql, Long id) throws SQLException {
        try (PreparedStatement statement = conexao.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    private Long inserirUsuario(String nome, String documento, String email,
                                TipoUsuario tipo, BigDecimal saldo) throws SQLException {
        String sql = """
                INSERT INTO usuarios (nome_completo, documento, email, tipo, saldo)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """;
        try (Connection conexao = CONEXAO.criar();
             PreparedStatement statement = conexao.prepareStatement(sql)) {
            statement.setString(1, nome);
            statement.setString(2, documento);
            statement.setString(3, email);
            statement.setString(4, tipo.name());
            statement.setBigDecimal(5, saldo);
            try (var resultado = statement.executeQuery()) {
                resultado.next();
                return resultado.getLong("id");
            }
        }
    }
}