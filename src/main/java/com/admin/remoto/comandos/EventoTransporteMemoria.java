package com.admin.remoto.comandos;

import com.admin.remoto.models.Evento;

import javax.swing.*;
import java.util.function.Consumer;

public class EventoTransporteMemoria implements EventoTransporte {
    private Consumer<Evento> receiver;

    @Override
    public void send(Evento e) {
        // simula latencia…
        SwingUtilities.invokeLater(() -> {
            if (receiver != null) receiver.accept(e);
        });
    }

    @Override
    public void onReceive(Consumer<Evento> handler) {
        this.receiver = handler;
    }
}