package com.admin.remoto.controller;


import com.admin.remoto.services.business.FileSelector;
import com.admin.remoto.services.business.FileSender;
import com.admin.remoto.services.business.SessionManager;
import com.admin.remoto.models.*;
import com.admin.remoto.services.panel.AdministracionService;
import com.admin.remoto.services.persistence.LogLoteService;
import com.admin.remoto.services.persistence.SesionService;
import com.admin.remoto.swing.AdministracionPanel;
import com.admin.remoto.websocket.Evento;
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
import java.util.function.Consumer;


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
    private Sesion currentSesion;
    private Servidor currentServidor;

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

    public void conectarAServidor(Servidor servidor, Consumer<Boolean> callback) {
        String host = servidor.getDireccion();
        int port = Integer.parseInt(servidor.getPuerto());

        // 1) Validar si la dirección ya está en uso
        if (sessionManager.direccionOcupada(host, port)) {
            System.out.println("REPETIDA");
            // Informa al panel (o quien sea) que hubo un error y devolvemos false
            callback.accept(false);
            return;
        }

        // 2) Realizar la conexión en segundo plano
        new SwingWorker<Void, Void>() {
            private String errorMessage;
            private boolean conexionExitosa = false;

            @Override
            protected Void doInBackground() {
                System.out.println("[DEBUG] Iniciando conexión...");
                try {
                    service.conectar(host, port);
                    System.out.println("[DEBUG] Conexión completada.");
                    conexionExitosa = true;
                } catch (Exception ex) {
                    errorMessage = ex.getMessage();
                    System.out.println("[DEBUG] Error al conectar: " + errorMessage);
                    conexionExitosa = false;
                }
                return null;
            }

            @Override
            protected void done() {
                if (!conexionExitosa || errorMessage != null) {
                    // Hubo error al conectar con WebSocket
                    panel.mostrarError("Error al conectar: " + errorMessage);
                    callback.accept(false);
                } else {
                    // Conexión WebSocket exitosa: creamos y guardamos la Sesión
                    Sesion sesion = new Sesion();
                    sesion.setFechaHoraInicio(LocalDateTime.now());
                    sesion.setUsuario(sessionManager.getUsuario());
                    sesion.setServidor(servidor);

                    sesionService.guardar(sesion);
                    currentSesion = sesion;
                    currentServidor = servidor;
                    sessionManager.addSesion(sesion, servidor);

                    panel.mostrarMensaje("Conectado a " + host + ":" + port);
                    // Informamos que la conexión fue exitosa
                    callback.accept(true);
                }
            }
        }.execute();
    }

    public void desconectar(String host, int port) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {

                if (currentSesion != null) {
                    currentSesion.setFechaHoraFin(LocalDateTime.now());
                    sesionService.actualizar(currentSesion.getId(), currentSesion);
                }

                if (currentSesion != null) {
                    sessionManager.removeSesion(currentSesion);
                }

                // 3) La desconexión WebSocket (pool) recibe host/port:
                service.desconectar(host, port);

                return null;
            }

            @Override
            protected void done() {
                // Aquí muestras la vista de “volver a lista” (AdminPanel se encarga de cerrar su ventana, etc.)
                // por ejemplo panel.mostrarMensaje("Desconectado de " + host + ":" + port);
            }
        }.execute();
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
        try {
            if (img != null) {
                panel.actualizarImagen(img);
            } else {
                mostrarError("Imagen recibida no válida");
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error inesperado al actualizar imagen: " + e.getMessage());
        }
    }

    public void guardarLoteLogs() {
        if (bufferLogs.isEmpty()) return;
        if (currentSesion == null) return;

        long timestampFinMillis = System.currentTimeMillis();
        try {
            String jsonLote = mapper.writeValueAsString(bufferLogs);
            Usuario current = sessionManager.getUsuario();

            LogLote lote = new LogLote();
            lote.setTimestampInicio(millisToDateTime(timestampInicioLote));
            lote.setTimestampFin(millisToDateTime(timestampFinMillis));
            lote.setUsuario(current);
            lote.setContenidoJson(jsonLote);

            // Aquí ya no uso sessionManager.getSesion(), sino la que me pasaron
            lote.setSesion(currentSesion);

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
                fileSender.enviarArchivo(archivo,currentServidor);
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