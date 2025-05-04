package com.admin.remoto.services;

import com.admin.remoto.models.Evento;
import com.admin.remoto.controller.AdministracionController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;


@Service
public class AdministracionService {
    /**
     * Listener para eventos provenientes del WebSocket y errores
     */
    public interface Listener {
        void onOpen();
        void onTextMessage(String message);
        void onBinaryMessage(ByteBuffer data);
        void onClose(int code, String reason, boolean remote);
        void onError(Exception ex);
    }

    private WebSocketClient socket;
    private final ObjectMapper mapper = new ObjectMapper();
    private Listener listener;

    /**
     * Registra el listener para recibir callbacks
     */
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /**
     * Conecta al servidor WebSocket y delega eventos al listener
     */
    public void conectar(String host, int port) throws Exception {
        socket = new WebSocketClient(new URI("ws://" + host + ":" + port + "/ws")) {
            @Override
            public void onOpen(ServerHandshake sh) {
                if (listener != null) listener.onOpen();
            }

            @Override
            public void onMessage(String message) {
                if (listener != null) listener.onTextMessage(message);
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                if (listener != null) listener.onBinaryMessage(bytes);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                if (listener != null) listener.onClose(code, reason, remote);
            }

            @Override
            public void onError(Exception ex) {
                if (listener != null) listener.onError(ex);
            }
        };

        if (!socket.connectBlocking()) {
            throw new IOException("No se pudo establecer conexión con el servidor");
        }
    }

    /**
     * Cierra la conexión WebSocket
     */
    public void desconectar() {
        if (socket != null && socket.isOpen()) {
            socket.close();
        }
    }

    /**
     * Envía un evento convertido a JSON
     */
    public void enviarEvento(Evento evento) throws Exception {
        if (socket != null && socket.isOpen()) {
            String json = mapper.writeValueAsString(evento);
            socket.send(json);
        }
    }

    /**
     * Procesa imágenes recibidas y las convierte a BufferedImage
     */
    public BufferedImage procesarImagen(ByteBuffer buffer) throws IOException {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        return ImageIO.read(new ByteArrayInputStream(data));
    }
}