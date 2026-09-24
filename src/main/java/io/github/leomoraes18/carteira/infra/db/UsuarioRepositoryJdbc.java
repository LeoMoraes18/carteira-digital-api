package io.github.leomoraes18.carteira.infra.db;

import io.github.leomoraes18.carteira.aplicacao.UsuarioRepository;
import io.github.leomoraes18.carteira.dominio.Carteira;
import io.github.leomoraes18.carteira.dominio.TipoUsuario;
import io.github.leomoraes18.carteira.dominio.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public class UsuarioRepositoryJdbc implements UsuarioRepository {

    private static final String SQL_BUSCAR_POR_ID = """
            SELECT id, nome_completo, documento, email, tipo, saldo
            FROM usuarios
            WHERE id = ?
            """;

    private final ConexaoFactory conexoes;

    public UsuarioRepositoryJdbc(ConexaoFactory conexoes) {
        this.conexoes = Objects.requireNonNull(conexoes, "fábrica de conexões é obrigatória");
    }

    @Override
    public Optional<Usuario> buscarPorId(long id) {
        try (Connection conexao = conexoes.criar();
             PreparedStatement statement = conexao.prepareStatement(SQL_BUSCAR_POR_ID)) {
            statement.setLong(1, id);
            try (ResultSet resultado = statement.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapear(resultado));
            }
        } catch (SQLException e) {
            throw new ConexaoException("erro ao buscar usuário por id: " + id, e);
        }
    }

    private Usuario mapear(ResultSet resultado) throws SQLException {
        return new Usuario(
                resultado.getLong("id"),
                resultado.getString("nome_completo"),
                resultado.getString("documento"),
                resultado.getString("email"),
                TipoUsuario.valueOf(resultado.getString("tipo")),
                new Carteira(resultado.getBigDecimal("saldo")));
    }
}
