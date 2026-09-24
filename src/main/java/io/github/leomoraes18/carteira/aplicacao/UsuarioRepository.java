package io.github.leomoraes18.carteira.aplicacao;

import io.github.leomoraes18.carteira.dominio.Usuario;

import java.util.Optional;

public interface UsuarioRepository {

    Optional<Usuario> buscarPorId(long id);

    void salvar(Usuario usuario);
}
