package com.admin.remoto.comandos;

import com.admin.remoto.models.Evento;

import java.util.function.Consumer;

public interface EventoTransporte {
    void send(Evento e);
    void onReceive(Consumer<Evento> handler);
}
