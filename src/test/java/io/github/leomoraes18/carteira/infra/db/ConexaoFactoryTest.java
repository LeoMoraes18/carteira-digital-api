package io.github.leomoraes18.carteira.infra.db;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ConexaoFactoryTest {

    @Test
    void deveConectarAoBancoLocal() throws SQLException {
        ConexaoFactory factory = new ConexaoFactory(
                "jdbc:postgresql://localhost:5433/carteira_digital",
                "carteira",
                "carteira");

        try (Connection conexao = factory.criar()) {
            assertFalse(conexao.isClosed());
        }
    }
}