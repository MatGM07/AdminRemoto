package com.admin.remoto.websocket;

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
    // Máximo de instancias que el pool creará simultáneamente (ajustable)
    private static final int MAX_POOL_SIZE = 10;

    // Cola de clientes “idle” (cerrados): disponibles para reusar
    private final BlockingQueue<PooledWebSocketClient> availableClients = new LinkedBlockingQueue<>();

    // Mapa de clientes en uso → clave “host:port”
    private final Map<PooledWebSocketClient, String> inUseClients = new ConcurrentHashMap<>();

    /**
     * Obtiene un cliente configurado para la URI dada, reusando uno idle si existe,
     * o creando uno nuevo (hasta MAX_POOL_SIZE).
     */
    public synchronized PooledWebSocketClient borrowClient(String host, int port, Consumer<Evento> callback) throws Exception {
        String clave = host + ":" + port;

        // *** Verificamos que no haya YA un cliente prestado a la misma clave ***
        if (inUseClients.containsValue(clave)) {
            throw new IllegalStateException("Ya existe una conexión activa a " + clave);
        }

        PooledWebSocketClient client = null;

        // 1. Intentar sacar uno idle de la cola
        client = availableClients.poll();
        if (client != null) {
            // Reconfiguramos instancia existente
            client.reconfigure(new URI("ws://" + host + ":" + port + "/ws"), callback);
        } else {
            // 2. Si no hay idle y aún no llegamos a máximo, creamos uno nuevo
            if (inUseClients.size() + availableClients.size() < MAX_POOL_SIZE) {
                client = new PooledWebSocketClient(new URI("ws://" + host + ":" + port + "/ws"), callback);
            } else {
                // Pool lleno y no hay idle: podemos bloquear hasta que alguno regrese,
                // o lanzar excepción. Aquí lanzamos excepción para simplificar.
                throw new IllegalStateException("Límite de conexiones simultáneas alcanzado en el pool.");
            }
        }

        // Marcamos este cliente como “en uso” para la clave actual

        return client;
    }

    public synchronized void marcarClienteComoEnUso(PooledWebSocketClient client, String clave) {
        inUseClients.put(client, clave);
    }

    /**
     * Devuelve un cliente al pool:
     * - Lo cerramos si está abierto,
     * - Lo extraemos de inUseClients,
     * - Lo añadimos a availableClients listo para reusar.
     */
    public synchronized void returnClient(PooledWebSocketClient client) {
        if (client == null) return;

        // Extraer de "en uso"
        String clave = inUseClients.remove(client);
        if (clave == null) return; // No estaba en uso

        // Si no está abierto, no lo devolvemos al pool
        if (!client.isOpen()) {
            System.out.println("[Pool] Cliente no reutilizable descartado.");
            return;
        }

        // Si sigue abierto, cerramos
        try {
            client.closeBlocking(); // bloquea hasta cerrar
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Limpiar callback para evitar retener objetos
        client.reconfigure(URI.create("ws://dummy/"), null);

        // Y lo devolvemos a la cola de disponibles
        availableClients.offer(client);
    }

    /**
     * Método para “desechar” todo el pool, cerrando todos los clientes.
     * (En caso de shutdown global de la app.)
     */
    public synchronized void shutdownPool() {
        // Cerramos inUse primero
        for (PooledWebSocketClient client : new ArrayList<>(inUseClients.keySet())) {
            try {
                if (client.isOpen()) client.closeBlocking();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            inUseClients.remove(client);
        }
        // Cerramos los idle
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