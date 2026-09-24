package io.github.leomoraes18.carteira.infra.db;

import io.github.leomoraes18.carteira.aplicacao.Resposta;
import io.github.leomoraes18.carteira.dominio.TipoUsuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransferenciaUnitOfWorkTest {

    private static final ConexaoFactory CONEXAO = new ConexaoFactory(
            "jdbc:postgresql://localhost:5433/carteira_digital", "carteira", "carteira");

    private Long idPagador;
    private Long idRecebedor;

    @BeforeEach
    void configurar() throws SQLException {
        idPagador = inserirUsuario("Pagador Teste", "55555555555",
                "pagador.uow@email.com", TipoUsuario.COMUM, new BigDecimal("100.00"));
        idRecebedor = inserirUsuario("Recebedor Teste", "66666666666",
                "recebedor.uow@email.com", TipoUsuario.LOJISTA, BigDecimal.ZERO);
    }

    @AfterEach
    void limpar() throws SQLException {
        try (Connection conexao = CONEXAO.criar()) {
            executar(conexao, "DELETE FROM notificacoes_pendentes WHERE destinatario_id = ?", idRecebedor);
            executar(conexao, "DELETE FROM usuarios WHERE id = ?", idPagador);
            executar(conexao, "DELETE FROM usuarios WHERE id = ?", idRecebedor);
        }
    }

    @Test
    void deveCommitarDebitoCreditoENotificacaoJuntos() throws SQLException {
        TransferenciaUnitOfWork unitOfWork = new TransferenciaUnitOfWork(
                CONEXAO, (pagador, recebedor, valor) -> true);

        Resposta resposta = unitOfWork.executar(Map.of(
                "value", new BigDecimal("30.00"),
                "payer", new BigDecimal(idPagador),
                "payee", new BigDecimal(idRecebedor)));

        assertEquals(201, resposta.status());
        assertEquals(new BigDecimal("70.00"), buscarSaldo(idPagador));
        assertEquals(new BigDecimal("30.00"), buscarSaldo(idRecebedor));
        assertEquals(1, contarNotificacoesPendentes(idRecebedor));
    }

    @Test
    void deveReverterQuandoAutorizadorNega() throws SQLException {
        TransferenciaUnitOfWork unitOfWork = new TransferenciaUnitOfWork(
                CONEXAO, (pagador, recebedor, valor) -> false);

        Resposta resposta = unitOfWork.executar(Map.of(
                "value", new BigDecimal("30.00"),
                "payer", new BigDecimal(idPagador),
                "payee", new BigDecimal(idRecebedor)));

        assertEquals(422, resposta.status());
        assertEquals(new BigDecimal("100.00"), buscarSaldo(idPagador));
        assertEquals(BigDecimal.ZERO.setScale(2), buscarSaldo(idRecebedor));
        assertEquals(0, contarNotificacoesPendentes(idRecebedor));
    }

    @Test
    void deveReverterQuandoSaldoInsuficiente() throws SQLException {
        TransferenciaUnitOfWork unitOfWork = new TransferenciaUnitOfWork(
                CONEXAO, (pagador, recebedor, valor) -> true);

        Resposta resposta = unitOfWork.executar(Map.of(
                "value", new BigDecimal("500.00"),
                "payer", new BigDecimal(idPagador),
                "payee", new BigDecimal(idRecebedor)));

        assertEquals(422, resposta.status());
        assertEquals(new BigDecimal("100.00"), buscarSaldo(idPagador));
    }

    private BigDecimal buscarSaldo(Long id) throws SQLException {
        try (Connection conexao = CONEXAO.criar();
             PreparedStatement statement = conexao.prepareStatement(
                     "SELECT saldo FROM usuarios WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet resultado = statement.executeQuery()) {
                resultado.next();
                return resultado.getBigDecimal("saldo");
            }
        }
    }

    private int contarNotificacoesPendentes(Long destinatarioId) throws SQLException {
        try (Connection conexao = CONEXAO.criar();
             PreparedStatement statement = conexao.prepareStatement(
                     "SELECT COUNT(*) FROM notificacoes_pendentes WHERE destinatario_id = ?")) {
            statement.setLong(1, destinatarioId);
            try (ResultSet resultado = statement.executeQuery()) {
                resultado.next();
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