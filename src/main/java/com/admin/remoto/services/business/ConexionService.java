package com.admin.remoto.services.business;

import com.admin.remoto.Observador.Observable;
import com.admin.remoto.Observador.Observador;
import com.admin.remoto.services.panel.AdministracionService;
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
public class ConexionService {
    private final WebSocketClientPool clientPool;
    private final SessionManager sessionManager;
    private final AdministracionService administracionService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final ConcurrentMap<String, PooledWebSocketClient> clientesEnUso = new ConcurrentHashMap<>();

    @Autowired
    public ConexionService(SessionManager sessionManager,
                           AdministracionService administracionService,
                           WebSocketClientPool clientPool) {
        this.sessionManager = sessionManager;
        this.administracionService = administracionService;
        this.clientPool = clientPool;
    }

    public void connect(String host, int port) throws Exception {
        String clave = host + ":" + port;

        Consumer<Evento> reenvio = evento -> administracionService.recibirEventoPara(clave, evento);

        PooledWebSocketClient client = clientPool.borrowClient(host, port, reenvio);

        CompletableFuture<Boolean> connectionFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return client.connectBlocking();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }, executorService);

        boolean ok = connectionFuture.get(10, TimeUnit.SECONDS);
        if (!ok) {
            throw new IOException("No se pudo establecer conexión con " + clave);
        }

        clientesEnUso.put(clave, client);
        clientPool.marcarClienteComoEnUso(client, clave);
    }

    public void disconnect(String host, int port) {
        String clave = host + ":" + port;
        PooledWebSocketClient client = clientesEnUso.remove(clave);
        if (client != null) {
            executorService.submit(() -> {
                try {
                    if (client.isOpen()) {
                        client.closeBlocking();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                clientPool.returnClient(client);

                administracionService.eliminarObservador(clave);
            });
        }
    }

    public void solicitarVideoDesdeCliente(String host, int port) {
        String clave = host + ":" + port;
        PooledWebSocketClient client = clientesEnUso.get(clave);
        if (client != null && client.isOpen()) {
            try {
                client.send("{\"comando\": \"enviar_video\"}");
                System.out.println(">>> [WS] Solicitud de envío de video enviada al cliente: " + clave);
            } catch (Exception e) {
                System.err.println(">>> [ERROR] No se pudo enviar comando al cliente: " + e.getMessage());
            }
        }
    }
}

