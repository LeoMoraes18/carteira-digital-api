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

public class UsuarioRepositoryTransacional implements UsuarioRepository {

    private static final String SQL_BUSCAR_POR_ID_COM_TRAVA = """
            SELECT id, nome_completo, documento, email, tipo, saldo
            FROM usuarios
            WHERE id = ?
            FOR UPDATE
            """;

    private static final String SQL_ATUALIZAR_SALDO = """
            UPDATE usuarios
            SET saldo = ?
            WHERE id = ?
            """;

    private final Connection conexao;

    public UsuarioRepositoryTransacional(Connection conexao) {
        this.conexao = Objects.requireNonNull(conexao, "conexão é obrigatória");
    }

    @Override
    public Optional<Usuario> buscarPorId(long id) {
        try (PreparedStatement statement = conexao.prepareStatement(SQL_BUSCAR_POR_ID_COM_TRAVA)) {
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

    @Override
    public void salvar(Usuario usuario) {
        try (PreparedStatement statement = conexao.prepareStatement(SQL_ATUALIZAR_SALDO)) {
            statement.setBigDecimal(1, usuario.carteira().saldo());
            statement.setLong(2, usuario.id());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ConexaoException("erro ao salvar usuário: " + usuario.id(), e);
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