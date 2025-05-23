package com.admin.remoto.services.business;

import com.admin.remoto.Observador.Observable;
import com.admin.remoto.Observador.Observador;
import com.admin.remoto.models.Evento;
import com.admin.remoto.models.Sesion;
import com.admin.remoto.services.persistence.SesionService;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Component
public class ConexionService implements Observable {
    private WebSocketClient socket;
    private final List<Observador> observadores = new ArrayList<>();
    private final SessionManager sessionManager;

    @Autowired
    private final SesionService sesionService;

    @Autowired
    public ConexionService(SessionManager sessionManager, SesionService sesionService){
        this.sessionManager = sessionManager;
        this.sesionService = sesionService;
    }

    @Override
    public void agregarObservador(Observador observador) {
        observadores.add(observador);
    }

    @Override
    public void eliminarObservador(Observador observador) {
        observadores.remove(observador);
    }

    @Override
    public void notificarObservadores(Evento evento) {
        for (Observador obs : observadores) {
            obs.actualizar(evento);
        }
    }

    public void connect(String fullUrl) throws Exception {
        socket = new WebSocketClient(new URI(fullUrl)) {
            @Override
            public void onOpen(ServerHandshake sh) {
                notificarObservadores(new Evento(Evento.Tipo.OPEN, null));
            }

            @Override
            public void onMessage(String message) {
                notificarObservadores(new Evento(Evento.Tipo.TEXT, message));
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                notificarObservadores(new Evento(Evento.Tipo.BINARY, bytes));
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                notificarObservadores(new Evento(Evento.Tipo.CLOSE, reason));
            }

            @Override
            public void onError(Exception ex) {
                notificarObservadores(new Evento(Evento.Tipo.ERROR, ex));
            }
        };

        if (!socket.connectBlocking()) {
            throw new IOException("No se pudo establecer conexión con el servidor");
        }
    }
    public void disconnect() {
        if (socket != null && socket.isOpen()) {
            Sesion finalizada = sessionManager.getSesion();
            finalizada.setFechaHoraFin(LocalDateTime.now());
            sesionService.actualizar(finalizada.getId(),finalizada);
            sessionManager.clearServidor();
            sessionManager.clearSesion();
            socket.close();
        }
    }
}
