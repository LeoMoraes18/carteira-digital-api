package io.github.leomoraes18.carteira.infra.db;

import io.github.leomoraes18.carteira.aplicacao.Notificacao;
import io.github.leomoraes18.carteira.dominio.TipoUsuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilaNotificacoesJdbcTest {

    private static final ConexaoFactory CONEXAO = new ConexaoFactory(
            "jdbc:postgresql://localhost:5433/carteira_digital", "carteira", "carteira");

    private Long idUsuario;

    @BeforeEach
    void configurar() throws SQLException {
        idUsuario = inserirUsuario("Destinatario Teste", "77777777777",
                "destinatario.fila@email.com", TipoUsuario.LOJISTA, BigDecimal.ZERO);
    }

    @AfterEach
    void limpar() throws SQLException {
        try (Connection conexao = CONEXAO.criar()) {
            executar(conexao, "DELETE FROM notificacoes_pendentes WHERE destinatario_id = ?", idUsuario);
            executar(conexao, "DELETE FROM usuarios WHERE id = ?", idUsuario);
        }
    }

    @Test
    void deveListarNotificacoesPendentes() throws SQLException {
        FilaNotificacoesJdbc fila = new FilaNotificacoesJdbc(CONEXAO);
        Notificacao notificacao = new Notificacao(idUsuario, "destinatario.fila@email.com",
                new BigDecimal("15.00"));

        fila.agendar(notificacao);

        List<Notificacao> pendentes = fila.pendentes();
        assertTrue(pendentes.contains(notificacao));
    }

    @Test
    void deveRemoverApenasUmaNotificacaoAoRemover() throws SQLException {
        FilaNotificacoesJdbc fila = new FilaNotificacoesJdbc(CONEXAO);
        Notificacao notificacao = new Notificacao(idUsuario, "destinatario.fila@email.com",
                new BigDecimal("15.00"));

        fila.agendar(notificacao);
        fila.agendar(notificacao);

        fila.remover(notificacao);

        long restantes = fila.pendentes().stream().filter(n -> n.equals(notificacao)).count();
        assertEquals(1, restantes);
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