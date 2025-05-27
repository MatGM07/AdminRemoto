package com.admin.remoto.websocket;

public class Evento {
    public enum Tipo {
        OPEN, TEXT, BINARY, CLOSE, ERROR
    }

    private final Tipo tipo;
    private final Object contenido;

    public Evento(Tipo tipo, Object contenido) {
        this.tipo = tipo;
        this.contenido = contenido;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public Object getContenido() {
        return contenido;
    }
}
