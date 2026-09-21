package io.github.leomoraes18.carteira.dominio;

import java.math.BigDecimal;
import java.util.Objects;

public class Usuario {

    private final Long id;
    private final String nomeCompleto;
    private final String documento;
    private final String email;
    private final TipoUsuario tipo;
    private final Carteira carteira;

    public Usuario(Long id, String nomeCompleto, String documento, String email,
                   TipoUsuario tipo, Carteira carteira) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.documento = documento;
        this.email = email;
        this.tipo = Objects.requireNonNull(tipo, "Tipo é obrigatório");
        this.carteira = Objects.requireNonNull(carteira, "carteira é obrigatório");
    }

    public void enviar(BigDecimal valor) {
        if (!tipo.podeEnviar()) {
            throw new OperacaoNaoPermitidaException("usuários do tipo " + tipo + " não podem enviar transferêncis");
        }
        carteira.debitar(valor);
    }

    public void receber(BigDecimal valor) {
        carteira.creditar(valor);
    }

    public Long id() { return id; }
    public String nomeCompleto() { return nomeCompleto; }
    public String documento() { return documento; }
    public String email() { return  email; }
    public TipoUsuario tipo() { return tipo; }
    public Carteira carteira() { return carteira; }

}
