package com.admin.remoto.websocket;

import com.admin.remoto.swing.AdministracionPanel;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

public class ClienteWebSocket extends WebSocketClient {

    private final AdministracionPanel panel;

    public ClienteWebSocket(String host, int port, AdministracionPanel panel) throws Exception {
        super(new URI("ws://" + host + ":" + port + "/ws"));
        this.panel = panel;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        panel.log("SOCKET", "Conectado al servidor");
    }

    @Override
    public void onMessage(String message) {
        panel.log("SOCKET-RX", message);
        // Puedes extender esta lógica si hay comandos de control textual
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        try {
            panel.log("SOCKET-IMG", "Recibida imagen de " + bytes.remaining() + " bytes");
            panel.displayImage(bytes);
        } catch (Exception e) {
            panel.log("ERROR-IMG", "Error al procesar imagen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        panel.log("SOCKET", "Cerrado: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        panel.log("SOCKET-ERR", ex.getMessage());
    }
}
