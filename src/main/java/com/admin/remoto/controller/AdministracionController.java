package com.admin.remoto.controller;


import com.admin.remoto.Observador.Observador;
import com.admin.remoto.models.Evento;
import com.admin.remoto.services.AdministracionService;
import com.admin.remoto.services.ConexionService;
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
public class AdministracionController {

    private final AdministracionService service;
    private AdministracionPanel panel;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    public AdministracionController(AdministracionService service) {
        this.service = service;
        this.service.setController(this);
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

    public void mostrarMensaje(String mensaje) {
        panel.mostrarMensaje(mensaje);
    }

    public void mostrarError(String mensaje) {
        panel.mostrarError(mensaje);
    }

    public void recibirTexto(Map<String, String> jsonMsg, String raw) {
        if ("log".equals(jsonMsg.get("type"))) {
            String logMessage = jsonMsg.get("message");
            panel.log("LOG", logMessage);
        } else {
            String now = timeFormat.format(new Date());
            panel.log("MSG " + now, raw);
        }
    }

    public void recibirImagen(BufferedImage img) {
        if (img != null) {
            panel.actualizarImagen(img);
        } else {
            panel.mostrarError("Imagen recibida no válida");
        }
    }
}