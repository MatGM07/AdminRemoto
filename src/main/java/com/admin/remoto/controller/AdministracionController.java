package com.admin.remoto.controller;


import com.admin.remoto.Observador.Observador;
import com.admin.remoto.SessionManager;
import com.admin.remoto.models.Evento;
import com.admin.remoto.models.LogEntry;
import com.admin.remoto.models.LogLote;
import com.admin.remoto.models.Usuario;
import com.admin.remoto.services.AdministracionService;
import com.admin.remoto.services.ConexionService;
import com.admin.remoto.services.LogLoteService;
import com.admin.remoto.swing.AdministracionPanel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class AdministracionController {

    private final AdministracionService service;
    private AdministracionPanel panel;
    private final LogLoteService logLoteService;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final List<LogEntry> bufferLogs = Collections.synchronizedList(new ArrayList<>());
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ObjectMapper mapper = new ObjectMapper();
    private long timestampInicioLote = System.currentTimeMillis();
    private final SessionManager sessionManager;

    @Autowired
    public AdministracionController(AdministracionService service, LogLoteService logLoteService, SessionManager sessionManager) {
        this.service = service;
        this.logLoteService = logLoteService;
        this.service.setController(this);
        this.sessionManager = sessionManager;

        scheduler.scheduleAtFixedRate(this::guardarLoteLogs, 30, 30, TimeUnit.SECONDS);
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
            LogEntry entry = new LogEntry("log", logMessage);
            bufferLogs.add(entry);
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

    private void guardarLoteLogs() {
        if (bufferLogs.isEmpty()) return;

        long timestampFinMillis = System.currentTimeMillis();
        try {
            String jsonLote = mapper.writeValueAsString(bufferLogs);
            Usuario current = sessionManager.getUsuario();

            LogLote lote = new LogLote();
            lote.setTimestampInicio(millisToDateTime(timestampInicioLote));
            lote.setTimestampFin(millisToDateTime(timestampFinMillis));
            lote.setUsuario(current);
            lote.setContenidoJson(jsonLote);

            logLoteService.guardarLote(lote);

            bufferLogs.clear();
            timestampInicioLote = System.currentTimeMillis();
        } catch (JsonProcessingException e) {
            panel.mostrarError("Error al convertir logs a JSON: " + e.getMessage());
        }
    }

    private LocalDateTime millisToDateTime(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }
}