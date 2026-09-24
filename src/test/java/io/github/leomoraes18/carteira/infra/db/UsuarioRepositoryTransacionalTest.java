package io.github.leomoraes18.carteira.infra.db;

import io.github.leomoraes18.carteira.dominio.TipoUsuario;
import io.github.leomoraes18.carteira.dominio.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioRepositoryTransacionalTest {

    private static final ConexaoFactory CONEXAO = new ConexaoFactory(
            "jdbc:postgresql://localhost:5433/carteira_digital", "carteira", "carteira");

    private Long idInserido;

    @BeforeEach
    void configurar() throws SQLException {
        idInserido = inserirUsuario("Ana Teste", "33333333333", "ana.transacional@email.com",
                TipoUsuario.COMUM, new BigDecimal("100.00"));
    }

    @AfterEach
    void limpar() throws SQLException {
        try (Connection conexao = CONEXAO.criar();
             PreparedStatement statement = conexao.prepareStatement(
                     "DELETE FROM usuarios WHERE id = ?")) {
            statement.setLong(1, idInserido);
            statement.executeUpdate();
        }
    }

    @Test
    void deveLerUsuarioDentroDeUmaTransacao() throws SQLException {
        try (Connection conexao = CONEXAO.criar()) {
            conexao.setAutoCommit(false);
            UsuarioRepositoryTransacional repositorio = new UsuarioRepositoryTransacional(conexao);

            Optional<Usuario> encontrado = repositorio.buscarPorId(idInserido);

            assertTrue(encontrado.isPresent());
            assertEquals(new BigDecimal("100.00"), encontrado.get().carteira().saldo());
            conexao.commit();
        }
    }

    @Test
    @Timeout(5)
    void deveTravarLinhaAteTransacaoTerminar() throws Exception {
        CountDownLatch primeiraLeituraFeita = new CountDownLatch(1);
        CountDownLatch podeCommitar = new CountDownLatch(1);

        CompletableFuture<Void> primeiraTransacao = CompletableFuture.runAsync(() -> {
            try (Connection conexao = CONEXAO.criar()) {
                conexao.setAutoCommit(false);
                UsuarioRepositoryTransacional repositorio = new UsuarioRepositoryTransacional(conexao);
                repositorio.buscarPorId(idInserido);

                primeiraLeituraFeita.countDown();
                podeCommitar.await();

                conexao.commit();
            } catch (SQLException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        primeiraLeituraFeita.await();

        CompletableFuture<Long> segundaTransacao = CompletableFuture.supplyAsync(() -> {
            long inicio = System.currentTimeMillis();
            try (Connection conexao = CONEXAO.criar()) {
                conexao.setAutoCommit(false);
                UsuarioRepositoryTransacional repositorio = new UsuarioRepositoryTransacional(conexao);
                repositorio.buscarPorId(idInserido);
                conexao.commit();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return System.currentTimeMillis() - inicio;
        });

        Thread.sleep(300);
        podeCommitar.countDown();

        long tempoEsperandoTrava = segundaTransacao.get(4, TimeUnit.SECONDS);
        primeiraTransacao.get(4, TimeUnit.SECONDS);

        assertTrue(tempoEsperandoTrava >= 250,
                "a segunda leitura deveria ter esperado a primeira transação liberar a linha");
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