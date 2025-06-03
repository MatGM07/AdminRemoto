package com.admin.remoto.services.connection;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

@Component
public class WebSocketClientPool {

    private static final int MAX_POOL_SIZE = 10;

    private final BlockingQueue<PooledWebSocketClient> availableClients = new LinkedBlockingQueue<>();

    private final Map<PooledWebSocketClient, String> inUseClients = new ConcurrentHashMap<>();


    public synchronized PooledWebSocketClient borrowClient(String host, int port, Consumer<Evento> callback) throws Exception {
        String clave = host + ":" + port;

        if (inUseClients.containsValue(clave)) {
            throw new IllegalStateException("Ya existe una conexión activa a " + clave);
        }

        PooledWebSocketClient client = null;


        client = availableClients.poll();
        if (client != null) {
            client.reconfigure(new URI("ws://" + host + ":" + port + "/ws"), callback);
        } else {

            if (inUseClients.size() + availableClients.size() < MAX_POOL_SIZE) {
                client = new PooledWebSocketClient(new URI("ws://" + host + ":" + port + "/ws"), callback);
            } else {

                throw new IllegalStateException("Límite de conexiones simultáneas alcanzado en el pool.");
            }
        }



        return client;
    }

    public synchronized void marcarClienteComoEnUso(PooledWebSocketClient client, String clave) {
        inUseClients.put(client, clave);
    }


    public synchronized void returnClient(PooledWebSocketClient client) {
        if (client == null) return;


        String clave = inUseClients.remove(client);
        if (clave == null) return; // No estaba en uso


        if (!client.isOpen()) {
            System.out.println("[Pool] Cliente no reutilizable descartado.");
            return;
        }

        try {
            client.closeBlocking();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        client.reconfigure(URI.create("ws://dummy/"), null);

        availableClients.offer(client);
    }


    public synchronized void shutdownPool() {

        for (PooledWebSocketClient client : new ArrayList<>(inUseClients.keySet())) {
            try {
                if (client.isOpen()) client.closeBlocking();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            inUseClients.remove(client);
        }

        for (PooledWebSocketClient client : new ArrayList<>(availableClients)) {
            try {
                if (client.isOpen()) client.closeBlocking();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            availableClients.remove(client);
        }
    }
}