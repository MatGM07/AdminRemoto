package com.admin.remoto.controller;


import com.admin.remoto.services.business.FileSelector;
import com.admin.remoto.services.business.FileSender;
import com.admin.remoto.services.business.SessionManager;
import com.admin.remoto.models.*;
import com.admin.remoto.services.panel.AdministracionService;
import com.admin.remoto.services.persistence.LogLoteService;
import com.admin.remoto.services.persistence.SesionService;
import com.admin.remoto.swing.AdministracionPanel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
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
import java.util.*;


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
    private final SesionService sesionService;
    private final FileSender fileSender;
    private final FileSelector fileSelector;

    @Autowired
    public AdministracionController(AdministracionService service, LogLoteService logLoteService, SessionManager sessionManager, SesionService sesionService, FileSender fileSender, FileSelector fileSelector) {
        this.service = service;
        this.logLoteService = logLoteService;
        this.service.setController(this);
        this.sessionManager = sessionManager;
        this.sesionService = sesionService;
        this.fileSender = fileSender;
        this.fileSelector = fileSelector;

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
                    sessionManager.clearServidor();
                } else {
                    panel.mostrarMensaje("Conectado a " + host + ":" + port);
                    Sesion sesion = new Sesion();
                    sesion.setFechaHoraInicio(LocalDateTime.now());
                    sesion.setUsuario(sessionManager.getUsuario());
                    sesion.setServidor(sessionManager.getServidor());
                    sesionService.guardar(sesion);
                    sessionManager.setSesion(sesion);
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

    public void guardarLoteLogs() {
        if (bufferLogs.isEmpty()) return;
        if (sessionManager.getSesion()==null) return;

        long timestampFinMillis = System.currentTimeMillis();
        try {
            String jsonLote = mapper.writeValueAsString(bufferLogs);
            Usuario current = sessionManager.getUsuario();

            LogLote lote = new LogLote();
            lote.setTimestampInicio(millisToDateTime(timestampInicioLote));
            lote.setTimestampFin(millisToDateTime(timestampFinMillis));
            lote.setUsuario(current);
            lote.setContenidoJson(jsonLote);
            lote.setSesion(sessionManager.getSesion());

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

    public void seleccionarYTransferirArchivo(java.awt.Component parentComponent) {
        File archivo = fileSelector.seleccionarArchivo(parentComponent);

        if (archivo != null) {
            panel.log("INFO", "Archivo seleccionado: " + archivo.getAbsolutePath());
            try {
                fileSender.enviarArchivo(archivo);
                panel.log("INFO", "Archivo enviado correctamente.");
            } catch (Exception ex) {
                panel.log("ERROR", "Error al enviar el archivo: " + ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            panel.log("INFO", "Transferencia cancelada por el usuario.");
        }
    }
}