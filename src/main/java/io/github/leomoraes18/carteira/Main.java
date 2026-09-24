package io.github.leomoraes18.carteira;

import com.sun.net.httpserver.HttpServer;
import io.github.leomoraes18.carteira.aplicacao.TransferenciaController;
import io.github.leomoraes18.carteira.aplicacao.TransferenciaService;
import io.github.leomoraes18.carteira.aplicacao.UsuarioRepositoryEmMemoria;
import io.github.leomoraes18.carteira.dominio.Carteira;
import io.github.leomoraes18.carteira.dominio.TipoUsuario;
import io.github.leomoraes18.carteira.dominio.Usuario;
import io.github.leomoraes18.carteira.infra.web.TransferenciaHttpHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws IOException {
        int porta = 8080;

        UsuarioRepositoryEmMemoria usuarios = criarUsuariosDeExemplo();

        TransferenciaService transferencias = new TransferenciaService(
                (pagador, recebedor, valor) -> true,
                notificacao -> System.out.println("notificação agendada: " + notificacao)
        );

        TransferenciaController controller = new TransferenciaController(usuarios, transferencias);

        HttpServer servidor = HttpServer.create(new InetSocketAddress(porta), 0);
        servidor.createContext("/transfer", new TransferenciaHttpHandler(controller));
        servidor.setExecutor(null);
        servidor.start();

        System.out.println("Servidor rodando em http://localhost:" + porta);
    }

    private static UsuarioRepositoryEmMemoria criarUsuariosDeExemplo() {
        UsuarioRepositoryEmMemoria usuarios = new UsuarioRepositoryEmMemoria();

        usuarios.salvar(new Usuario(1L, "Ana Souza", "11111111111", "ana@email.com",
                TipoUsuario.COMUM, new Carteira(new BigDecimal("100.00"))));
        usuarios.salvar(new Usuario(2L, "Loja do João", "22222222222", "joao@email.com",
                TipoUsuario.LOJISTA, new Carteira(new BigDecimal("0.00"))));

        return usuarios;
    }
}
