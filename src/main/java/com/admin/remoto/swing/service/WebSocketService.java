package com.admin.remoto.swing.service;

import com.admin.remoto.swing.AdministracionPanel;
import com.admin.remoto.websocket.ClienteWebSocket;
import org.java_websocket.client.WebSocketClient;

public class WebSocketService {

    public static WebSocketClient conectarPorWebSocket(String host, int port, AdministracionPanel panel) throws Exception {
        // Crear una nueva instancia de ClienteWebSocket con la información proporcionada
        ClienteWebSocket socket = new ClienteWebSocket(host, port, panel);

        // Intentar conectar al servidor WebSocket de manera bloqueante
        boolean connected = socket.connectBlocking();

        // Si no se pudo conectar, lanzar una excepción
        if (!connected) {
            throw new Exception("No se pudo establecer conexión con el servidor");
        }

        // Retornar el socket conectado
        return socket;
    }

    // Este método puede ser utilizado para cerrar la conexión si fuera necesario
    public static void cerrarConexion(WebSocketClient socket) {
        if (socket != null && socket.isOpen()) {
            socket.close();
        }
    }
}
