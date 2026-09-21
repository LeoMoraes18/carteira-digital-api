package io.github.leomoraes18.carteira.dominio;

public enum TipoUsuario {
    COMUM(true),
    LOJISTA(false);

    private final boolean podeEnviar;

    TipoUsuario(boolean podeEnviar) {
        this.podeEnviar = podeEnviar;
    }

    public boolean podeEnviar() {
        return podeEnviar;
    }
}
