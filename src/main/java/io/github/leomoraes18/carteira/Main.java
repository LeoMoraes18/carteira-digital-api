package io.github.leomoraes18.carteira;

import io.github.leomoraes18.carteira.infra.db.ConexaoFactory;
import io.github.leomoraes18.carteira.infra.db.TransferenciaUnitOfWork;
import io.github.leomoraes18.carteira.infra.web.TransferenciaHttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws IOException {
        int porta = 8080;

        ConexaoFactory conexoes = new ConexaoFactory(
                "jdbc:postgresql://localhost:5433/carteira_digital", "carteira", "carteira");

        TransferenciaUnitOfWork unitOfWork = new TransferenciaUnitOfWork(
                conexoes,
                (pagador, recebedor, valor) -> true);

        HttpServer servidor = HttpServer.create(new InetSocketAddress(porta), 0);
        servidor.createContext("/transfer", new TransferenciaHttpHandler(unitOfWork));
        servidor.setExecutor(null);
        servidor.start();

        System.out.println("Servidor rodando em http://localhost:" + porta);
    }
}