package io.github.leomoraes18.carteira.aplicacao;

import io.github.leomoraes18.carteira.dominio.Usuario;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class UsuarioRepositoryEmMemoria implements UsuarioRepository {

    private final Map<Long, Usuario> usuarios = new ConcurrentHashMap<>();

    public void salvar(Usuario usuario) {
        usuarios.put(usuario.id(), usuario);
    }

    @Override
    public Optional<Usuario> buscarPorId(long id) {
        return Optional.ofNullable(usuarios.get(id));
    }
}
