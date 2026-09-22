package io.github.leomoraes18.carteira.infra.web;

public class JsonParseException extends RuntimeException {
    public JsonParseException(String message) {
        super(message);
    }
}
