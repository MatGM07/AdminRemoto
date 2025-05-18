package com.admin.remoto.websocket;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.admin.remoto.services.AdministracionService;
import com.admin.remoto.swing.AdministracionPanel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.imageio.ImageIO;
import javax.swing.*;
@Component
public class MyWebSocketHandler implements WebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ExecutorService procesadorImagenes = Executors.newSingleThreadExecutor();

    @Autowired
    private AdministracionService administracionService;

    @Autowired
    private AdministracionPanel administracionPanel;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("Cliente conectado: " + session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        if (message instanceof BinaryMessage) {
            ByteBuffer buffer = ((BinaryMessage) message).getPayload();
            procesadorImagenes.submit(() -> {
                try {
                    BufferedImage img = administracionService.procesarImagen(buffer);
                    administracionPanel.actualizarImagen(img);
                } catch (IOException e) {
                    System.err.println("Error al procesar imagen: " + e.getMessage());
                }
            });
        } else if (message instanceof TextMessage) {
            // Opcional: manejar mensajes de texto también si los usas
            System.out.println("Texto recibido: " + ((TextMessage) message).getPayload());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.println("Error de transporte: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        sessions.remove(session);
        System.out.println("Cliente desconectado: " + session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}