package io.github.leomoraes18.carteira.infra.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Json {

    private Json() {
    }

    public static Object parse(String texto) {
        Parser parser = new Parser(texto);
        Object valor = parser.parseValor();
        parser.pularEspacos();
        if (!parser.fimDoTexto()) {
            throw new JsonParseException("texto sobrando após o JSON");
        }
        return valor;
    }

    public static String write(Object valor) {
        StringBuilder saida = new StringBuilder();
        escrever(valor, saida);
        return saida.toString();
    }

    private static void escrever(Object valor, StringBuilder saida) {
        switch (valor) {
            case null -> saida.append("null");
            case String texto -> escreverString(texto, saida);
            case BigDecimal numero -> saida.append(numero.toPlainString());
            case Boolean booleano -> saida.append(booleano);
            case Map<?, ?> objeto -> escreverObjeto(objeto, saida);
            case List<?> lista -> escreverLista(lista, saida);
            default -> throw new IllegalArgumentException(
                    "tipo não suportado para JSON: " + valor.getClass());
        }
    }

    private static void escreverObjeto(Map<?, ?> objeto, StringBuilder saida) {
        saida.append('{');
        boolean primeiro = true;
        for (Map.Entry<?, ?> entrada : objeto.entrySet()) {
            if (!primeiro) {
                saida.append(',');
            }
            primeiro = false;
            escreverString(String.valueOf(entrada.getKey()), saida);
            saida.append(':');
            escrever(entrada.getValue(), saida);
        }
        saida.append('}');
    }

    private static void escreverLista(List<?> lista, StringBuilder saida) {
        saida.append('[');
        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) {
                saida.append(',');
            }
            escrever(lista.get(i), saida);
        }
        saida.append(']');
    }

    private static void escreverString(String texto, StringBuilder saida) {
        saida.append('"');
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '"' -> saida.append("\\\"");
                case '\\' -> saida.append("\\\\");
                case '\n' -> saida.append("\\n");
                default -> saida.append(c);
            }
        }
        saida.append('"');
    }

    private static final class Parser {

        private final String texto;
        private int posicao;

        private Parser(String texto) {
            this.texto = texto;
        }

        boolean fimDoTexto() {
            return posicao >= texto.length();
        }

        Object parseValor() {
            pularEspacos();
            if (fimDoTexto()) {
                throw new JsonParseException("fim inesperado do JSON");
            }
            char atual = texto.charAt(posicao);
            return switch (atual) {
                case '{' -> parseObjeto();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't', 'f' -> parseBooleano();
                case 'n' -> parseNulo();
                default -> parseNumero();
            };
        }

        private Map<String, Object> parseObjeto() {
            Map<String, Object> objeto = new LinkedHashMap<>();
            esperar('{');
            pularEspacos();
            if (proximoCaractereE('}')) {
                posicao++;
                return objeto;
            }
            while (true) {
                pularEspacos();
                String chave = parseString();
                pularEspacos();
                esperar(':');
                Object valor = parseValor();
                objeto.put(chave, valor);
                pularEspacos();
                if (proximoCaractereE(',')) {
                    posicao++;
                } else {
                    esperar('}');
                    return objeto;
                }
            }
        }

        private List<Object> parseArray() {
            List<Object> lista = new ArrayList<>();
            esperar('[');
            pularEspacos();
            if (proximoCaractereE(']')) {
                posicao++;
                return lista;
            }
            while (true) {
                lista.add(parseValor());
                pularEspacos();
                if (proximoCaractereE(',')) {
                    posicao++;
                } else {
                    esperar(']');
                    return lista;
                }
            }
        }

        private String parseString() {
            esperar('"');
            StringBuilder valor = new StringBuilder();
            while (true) {
                if (fimDoTexto()) {
                    throw new JsonParseException("string não fechada");
                }
                char c = texto.charAt(posicao++);
                if (c == '"') {
                    return valor.toString();
                }
                if (c == '\\') {
                    valor.append(escaparProximo());
                } else {
                    valor.append(c);
                }
            }
        }

        private char escaparProximo() {
            char escapado = texto.charAt(posicao++);
            return switch (escapado) {
                case '"' -> '"';
                case '\\' -> '\\';
                case 'n' -> '\n';
                case 't' -> '\t';
                default -> throw new JsonParseException("sequência de escape inválida: \\" + escapado);
            };
        }

        private Boolean parseBooleano() {
            if (texto.startsWith("true", posicao)) {
                posicao += 4;
                return Boolean.TRUE;
            }
            if (texto.startsWith("false", posicao)) {
                posicao += 5;
                return Boolean.FALSE;
            }
            throw new JsonParseException("valor booleano inválido");
        }

        private Object parseNulo() {
            if (texto.startsWith("null", posicao)) {
                posicao += 4;
                return null;
            }
            throw new JsonParseException("valor inválido");
        }

        private BigDecimal parseNumero() {
            int inicio = posicao;
            if (proximoCaractereE('-')) {
                posicao++;
            }
            while (!fimDoTexto() && (Character.isDigit(texto.charAt(posicao))
                    || texto.charAt(posicao) == '.')) {
                posicao++;
            }
            if (posicao == inicio) {
                throw new JsonParseException("número inválido na posição " + posicao);
            }
            return new BigDecimal(texto.substring(inicio, posicao));
        }

        void pularEspacos() {
            while (!fimDoTexto() && Character.isWhitespace(texto.charAt(posicao))) {
                posicao++;
            }
        }

        private boolean proximoCaractereE(char esperado) {
            return !fimDoTexto() && texto.charAt(posicao) == esperado;
        }

        private void esperar(char esperado) {
            if (!proximoCaractereE(esperado)) {
                throw new JsonParseException("esperado '" + esperado + "' na posição " + posicao);
            }
            posicao++;
        }
    }
}