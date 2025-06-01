package com.admin.remoto.websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class PooledWebSocketClient extends WebSocketClient {
    // Callback interno que delega a ConexionService para notificar a sus observadores
    private Consumer<Evento> eventoCallback;

    public PooledWebSocketClient(URI serverUri, Consumer<Evento> eventoCallback) {
        super(serverUri);
        this.eventoCallback = eventoCallback;
    }

    /** Permite reasignar la URI y el callback antes de conectar. */
    public void reconfigure(URI newUri, Consumer<Evento> newCallback) {
        this.eventoCallback = newCallback;
        this.uri = newUri; // 'uri' es protected en WebSocketClient
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        if (eventoCallback != null) {
            eventoCallback.accept(new Evento(Evento.Tipo.OPEN, null));
        }
    }

    @Override
    public void onMessage(String message) {
        if (eventoCallback != null) {
            eventoCallback.accept(new Evento(Evento.Tipo.TEXT, message));
        }
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        if (eventoCallback != null) {
            eventoCallback.accept(new Evento(Evento.Tipo.BINARY, bytes));
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (eventoCallback != null) {
            eventoCallback.accept(new Evento(Evento.Tipo.CLOSE, reason));
        }
    }

    @Override
    public void onError(Exception ex) {
        if (eventoCallback != null) {
            eventoCallback.accept(new Evento(Evento.Tipo.ERROR, ex));
        }
    }
}