package com.admin.remoto.services.business;

import com.admin.remoto.Observador.Observable;
import com.admin.remoto.Observador.Observador;
import com.admin.remoto.websocket.Evento;
import com.admin.remoto.models.Sesion;
import com.admin.remoto.services.persistence.SesionService;
import com.admin.remoto.websocket.PooledWebSocketClient;
import com.admin.remoto.websocket.WebSocketClientPool;
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
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;


@Component
public class ConexionService implements Observable<Evento,Void> {
    private WebSocketClient socket;
    private final WebSocketClientPool clientPool;
    private final Map<String, PooledWebSocketClient> clientesEnUso = new ConcurrentHashMap<>();
    private final List<Observador> observadores = new ArrayList<>();
    private final SessionManager sessionManager;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Autowired
    private final SesionService sesionService;

    @Autowired
    public ConexionService(SessionManager sessionManager, SesionService sesionService, WebSocketClientPool clientPool) {
        this.sessionManager = sessionManager;
        this.sesionService = sesionService;
        this.clientPool = clientPool;
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
    public void notificarObservadores(Evento evento, Void v) {
        for (Observador obs : observadores) {
            obs.actualizar(evento, v);
        }
    }

    public void connect(String host, int port) throws Exception {
        String clave = host + ":" + port;

        // Pedimos un cliente del pool (échara excepción si ya hay uno activo en la misma clave).
        Consumer<Evento> reenvio = evento -> {
            System.out.println("[DEBUG] Evento recibido: " + evento.getTipo());
            notificarObservadores(evento, null);
        };

        PooledWebSocketClient client = clientPool.borrowClient(host, port, reenvio);

        CompletableFuture<Boolean> connectionFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return client.connectBlocking(); // Esto se ejecuta en un hilo separado
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }, executorService);

        boolean ok = connectionFuture.get(10, TimeUnit.SECONDS);

        if (!ok) {
            // ¡No devolver al pool!
            throw new IOException("No se pudo establecer conexión con " + clave);
        }
        clientesEnUso.put(clave, client);
        clientPool.marcarClienteComoEnUso(client, clave);
    }


    public void disconnect(String host, int port) {
        String clave = host + ":" + port;
        System.out.println("[DEBUG] Solicitando desconexión para " + clave);

        PooledWebSocketClient client = clientesEnUso.remove(clave);
        if (client == null) {
            System.out.println("[DEBUG] No hay cliente registrado en uso para " + clave);
            return;
        }

        executorService.submit(() -> {
            System.out.println("[DEBUG] Ejecutando desconexión async para " + clave);
            if (client.isOpen()) {
                try {
                    client.closeBlocking();
                    System.out.println("[DEBUG] Cerrando cliente para " + clave);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("[DEBUG] InterruptedException al cerrar cliente");
                }
            } else {
                System.out.println("[DEBUG] Cliente ya estaba cerrado: " + clave);
            }

            clientPool.returnClient(client);
        });
    }

}

