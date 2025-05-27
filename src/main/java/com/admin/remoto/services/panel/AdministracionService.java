package com.admin.remoto.services.panel;

import com.admin.remoto.Observador.Observador;
import com.admin.remoto.controller.AdministracionController;
import com.admin.remoto.websocket.Evento;
import com.admin.remoto.services.business.ConexionService;
import com.admin.remoto.services.business.ImageReceiver;
import com.admin.remoto.services.business.LogsReceiver;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

@Service
public class AdministracionService implements Observador<Evento, Void> {

    private final ConexionService conexionService;
    private final LogsReceiver logsReceiver;
    private final ImageReceiver imageProcessor;
    private AdministracionController controller;

    public AdministracionService(ConexionService conexionService, LogsReceiver logsReceiver, ImageReceiver imageProcessor) {
        this.conexionService = conexionService;
        this.logsReceiver = logsReceiver;
        this.imageProcessor = imageProcessor;
        this.conexionService.agregarObservador(this);
    }

    public void setController(AdministracionController controller) {
        this.controller = controller;
    }

    @Override
    public void actualizar(Evento evento, Void v) {
        switch (evento.getTipo()) {
            case OPEN -> controller.mostrarMensaje("Conexión abierta");
            case TEXT -> {
                String msg = (String) evento.getContenido();
                Map<String, String> datos = procesarMensajeJson(msg);
                controller.recibirTexto(datos, msg);
            }
            case BINARY -> {
                try {
                    BufferedImage img = procesarImagen((ByteBuffer) evento.getContenido());
                    controller.recibirImagen(img);
                } catch (IOException e) {
                    controller.mostrarError("Error al procesar imagen: " + e.getMessage());
                }
            }
            case CLOSE -> controller.mostrarMensaje("Conexión cerrada: " + evento.getContenido());
            case ERROR -> {
                Exception ex = (Exception) evento.getContenido();
                controller.mostrarError("Error WebSocket: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public void conectar(String host, int port) {
        try {
            conexionService.connect("ws://" + host + ":" + port + "/ws");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void desconectar() {
        controller.guardarLoteLogs();
        conexionService.disconnect();
    }

    public Map<String, String> procesarMensajeJson(String json) {
        return logsReceiver.parse(json);
    }

    public BufferedImage procesarImagen(ByteBuffer buffer) throws IOException {
        return imageProcessor.fromBuffer(buffer);
    }
}