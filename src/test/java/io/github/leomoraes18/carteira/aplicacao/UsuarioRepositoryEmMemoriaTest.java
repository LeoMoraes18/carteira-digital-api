package io.github.leomoraes18.carteira.aplicacao;

import io.github.leomoraes18.carteira.dominio.Carteira;
import io.github.leomoraes18.carteira.dominio.TipoUsuario;
import io.github.leomoraes18.carteira.dominio.Usuario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioRepositoryEmMemoriaTest {

    @Test
    void deveEncontrarUsuarioCadastrado() {
        UsuarioRepositoryEmMemoria repositorio = new UsuarioRepositoryEmMemoria();

        Usuario usuario = new Usuario(1L, "Anna Souza", "12345678900", "ana@email.com",
                TipoUsuario.COMUM, new Carteira(new BigDecimal("100.00")));

        repositorio.salvar(usuario);

        Optional<Usuario> encontrado = repositorio.buscarPorId(1L);

        assertTrue(encontrado.isPresent());
        assertEquals("Anna Souza", encontrado.get().nomeCompleto());
    }

    @Test
    void deveRetornarVazioQuandoNaoEncontrado() {
        UsuarioRepositoryEmMemoria repositorio = new UsuarioRepositoryEmMemoria();

        Optional<Usuario> encontrado = repositorio.buscarPorId(999L);

        assertTrue(encontrado.isEmpty());
    }

}
