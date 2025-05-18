package com.admin.remoto.controller;


import com.admin.remoto.services.AdministracionService;
import com.admin.remoto.swing.AdministracionPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Component
public class AdministracionController implements AdministracionService.Listener {
    private final AdministracionService service;
    private AdministracionPanel panel;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    public AdministracionController(AdministracionService service) {
        this.service = service;
        this.service.setListener(this);
    }

    public void setAdministracionPanel(AdministracionPanel panel) {
        this.panel = panel;
    }

    public void conectarAServidor(String host, int port) {
        new SwingWorker<Void, Void>() {
            private String errorMessage;

            @Override
            protected Void doInBackground() {
                try {
                    service.conectar(host, port);
                } catch (Exception ex) {
                    errorMessage = ex.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                if (errorMessage != null) {
                    panel.mostrarError("Error al conectar: " + errorMessage);
                    panel.volverAListaServidores();
                } else {
                    panel.mostrarMensaje("Conectado a " + host + ":" + port);
                }
            }
        }.execute();
    }

    public void desconectar() {
        service.desconectar();
    }

    // --- Implementación de la interfaz Listener ---
    @Override
    public void onOpen() {
        panel.mostrarMensaje("WebSocket abierto - Esperando datos del cliente...");
    }

    @Override
    public void onTextMessage(String message) {
        try {
            // Procesar el mensaje como JSON
            Map<String, String> jsonMsg = service.procesarMensajeJson(message);

            if (jsonMsg.containsKey("type") && "log".equals(jsonMsg.get("type"))) {
                // Es un mensaje de log del WindowTracker
                String logMessage = jsonMsg.get("message");
                panel.log("LOG", logMessage);

                // También lo enviamos a la consola del servidor
                System.out.println(logMessage);
            } else {
                // Es otro tipo de mensaje de texto
                String now = timeFormat.format(new Date());
                panel.log("MSG " + now, message);
            }
        } catch (Exception e) {
            // Si hay error al procesar el JSON, mostramos el mensaje en bruto
            panel.log("RX-TXT", message);
        }
    }

    @Override
    public void onBinaryMessage(ByteBuffer data) {
        try {
            BufferedImage img = service.procesarImagen(data);
            if (img != null) {
                panel.actualizarImagen(img);
            } else {
                panel.mostrarError("Imagen recibida no válida");
            }
        } catch (IOException ex) {
            panel.mostrarError("Error al procesar imagen: " + ex.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        String mensaje = "WebSocket cerrado";
        if (remote) {
            mensaje += " por el cliente";
        }
        mensaje += ": " + reason + " (código: " + code + ")";
        panel.mostrarMensaje(mensaje);
    }

    @Override
    public void onError(Exception ex) {
        panel.mostrarError("WebSocket error: " + ex.getMessage());
        ex.printStackTrace();
    }
}