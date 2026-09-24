package io.github.leomoraes18.carteira.infra.db;

import io.github.leomoraes18.carteira.dominio.Carteira;
import io.github.leomoraes18.carteira.dominio.TipoUsuario;
import io.github.leomoraes18.carteira.dominio.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioRepositoryJdbcTest {

    private static final ConexaoFactory CONEXAO = new ConexaoFactory(
            "jdbc:postgresql://localhost:5433/carteira_digital", "carteira", "carteira"
    );
    private UsuarioRepositoryJdbc repositorio;
    private long idInserido;

    @BeforeEach
    void configurar() throws SQLException {
        repositorio = new UsuarioRepositoryJdbc(CONEXAO);
        idInserido = inserirUsuario("Anna Souza", "12345678900", "ana.teste@email.com",
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
    void deveEncontrarUsuarioCadastrado() {
        Optional<Usuario> encontrado = repositorio.buscarPorId(idInserido);

        assertTrue(encontrado.isPresent());
        Usuario usuario = encontrado.get();
        assertEquals("Anna Souza", usuario.nomeCompleto());
        assertEquals(TipoUsuario.COMUM, usuario.tipo());
        assertEquals(new BigDecimal("100.00"), usuario.carteira().saldo());
    }

    @Test
    void deveRetornarVazioQuandoNaoEncontrado() {
        Optional<Usuario> encontrado = repositorio.buscarPorId(999_999L);

        assertTrue(encontrado.isEmpty());
    }

    @Test
    void deveAtualizarSaldoAoSalvar() {
        Optional<Usuario> antes = repositorio.buscarPorId(idInserido);
        Usuario usuario = antes.orElseThrow();

        usuario.carteira().creditar(new BigDecimal("50.00"));
        repositorio.salvar(usuario);

        Optional<Usuario> depois = repositorio.buscarPorId(idInserido);
        assertEquals(new BigDecimal("150.00"), depois.orElseThrow().carteira().saldo());
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
