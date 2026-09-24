package io.github.leomoraes18.carteira.infra.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexaoFactory {

    private final String url;
    private final String usuario;
    private final String senha;

    public ConexaoFactory(String url, String usuario, String senha) {
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
    }

    public Connection criar() {
        try {
            return DriverManager.getConnection(url, usuario, senha);
        } catch (SQLException e) {
            throw new ConexaoException("não foi possível conectar ao banco de dados", e);
        }
    }
}
