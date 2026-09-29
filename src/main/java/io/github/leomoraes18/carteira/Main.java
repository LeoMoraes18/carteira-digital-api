package io.github.leomoraes18.carteira;

import io.github.leomoraes18.carteira.aplicacao.ProcessadorNotificacoes;
import io.github.leomoraes18.carteira.infra.db.ConexaoFactory;
import io.github.leomoraes18.carteira.infra.db.FilaNotificacoesJdbc;
import io.github.leomoraes18.carteira.infra.db.TransferenciaUnitOfWork;
import io.github.leomoraes18.carteira.infra.http.NotificadorHttp;
import io.github.leomoraes18.carteira.infra.web.TransferenciaHttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws IOException {
        int porta = 8080;

        String dbUrl = System.getenv().getOrDefault("DB_URL",
                "jdbc:postgresql://localhost:5433/carteira_digital");
        String dbUser = System.getenv().getOrDefault("DB_USER", "carteira");
        String dbPassword = System.getenv().getOrDefault("DB_PASSWORD", "carteira");

        ConexaoFactory conexoes = new ConexaoFactory(dbUrl, dbUser, dbPassword);

        TransferenciaUnitOfWork unitOfWork = new TransferenciaUnitOfWork(
                conexoes,
                (pagador, recebedor, valor) -> true);

        HttpServer servidor = HttpServer.create(new InetSocketAddress(porta), 0);
        servidor.createContext("/transfer", new TransferenciaHttpHandler(unitOfWork));
        servidor.setExecutor(null);
        servidor.start();

        System.out.println("Servidor rodando em http://localhost:" + porta);

        iniciarProcessamentoDeNotificacoes(conexoes);
    }

    private static void iniciarProcessamentoDeNotificacoes(ConexaoFactory conexoes) {
        FilaNotificacoesJdbc fila = new FilaNotificacoesJdbc(conexoes);
        NotificadorHttp notificador = new NotificadorHttp("https://util.devi.tools/api/v1/notify");
        ProcessadorNotificacoes processador = new ProcessadorNotificacoes(fila, notificador);

        ScheduledExecutorService agendador = Executors.newSingleThreadScheduledExecutor();
        agendador.scheduleAtFixedRate(processador::processarPendentes, 10, 10, TimeUnit.SECONDS);

        System.out.println("Processamento de notificações agendado a cada 10 segundos");
    }
}