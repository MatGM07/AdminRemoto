package com.admin.remoto.controller;

import com.admin.remoto.EventoConverter;
import com.admin.remoto.models.Evento;
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

@Component
public class AdministracionController implements AdministracionService.Listener {
    private final AdministracionService service;
    private AdministracionPanel panel;
    private AWTEventListener awtListener;

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
                    registerAWTListener();
                }
            }
        }.execute();
    }

    /**
     * Registra el listener AWT en el UI y delega eventos al controlador
     */
    private void registerAWTListener() {
        long mask = AWTEvent.KEY_EVENT_MASK |
                AWTEvent.MOUSE_EVENT_MASK |
                AWTEvent.MOUSE_MOTION_EVENT_MASK |
                AWTEvent.MOUSE_WHEEL_EVENT_MASK;
        awtListener = event -> manejarEventoAWT((AWTEvent) event);
        Toolkit.getDefaultToolkit().addAWTEventListener(awtListener, mask);
    }

    public void desconectar() {
        service.desconectar();
        if (awtListener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(awtListener);
        }
    }

    /**
     * Captura eventos AWT, los convierte y los envía al servidor
     */
    public void manejarEventoAWT(AWTEvent event) {
        try {
            Evento re = EventoConverter.convert(event);
            if (re != null) {
                service.enviarEvento(re);
                panel.log("TX-EVT", re.toString());
            }
        } catch (Exception ex) {
            panel.log("ERROR-EVT", ex.getMessage());
        }
    }

    // --- Implementación de la interfaz Listener ---
    @Override
    public void onOpen() {
        panel.mostrarMensaje("WebSocket abierto");
    }

    @Override
    public void onTextMessage(String message) {
        panel.log("RX-TXT", message);
    }

    @Override
    public void onBinaryMessage(ByteBuffer data) {
        try {
            BufferedImage img = service.procesarImagen(data);
            if (img != null) panel.actualizarImagen(img);
        } catch (IOException ex) {
            panel.log("ERROR-IMG", ex.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        panel.mostrarMensaje("WebSocket cerrado: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        panel.mostrarError("WebSocket error: " + ex.getMessage());
    }
}