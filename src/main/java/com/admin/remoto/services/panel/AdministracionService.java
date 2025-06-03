package com.admin.remoto.services.panel;

import com.admin.remoto.Observador.Observador;
import com.admin.remoto.services.connection.Evento;
import com.admin.remoto.services.render.ImageReceiver;
import com.admin.remoto.services.logger.LogsReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AdministracionService {
    private final LogsReceiver logsReceiver;
    private final ImageReceiver imageProcessor;


    private final ConcurrentMap<String, Observador<Evento, Void>> observadoresPorClave = new ConcurrentHashMap<>();

    @Autowired
    public AdministracionService(LogsReceiver logsReceiver, ImageReceiver imageProcessor) {
        this.logsReceiver = logsReceiver;
        this.imageProcessor = imageProcessor;

    }

    /** Llamar cuando un controlador quiere registrarse para recibir eventos de `host:port`. */
    public void agregarObservador(String clave, Observador<Evento, Void> obs) {
        observadoresPorClave.put(clave, obs);
    }

    /** Llamar cuando un controlador cierra su ventana y deja de querer eventos. */
    public void eliminarObservador(String clave) {
        observadoresPorClave.remove(clave);
    }

    /**
     * Este método se invoca internamente desde el callback de WebSocket (en ConexionService),
     * indicando “llegó este evento para `clave`”.
     * Aquí miramos si hay un Observador registrado para esa clave, y si lo hay, le enviamos el evento.
     */
    public void recibirEventoPara(String clave, Evento evento) {
        Observador<Evento, Void> obs = observadoresPorClave.get(clave);
        if (obs != null) {
            obs.actualizar(evento, null);
        }
    }

    public Map<String, String> procesarMensajeJson(String json) {
        return logsReceiver.parse(json);
    }



    public BufferedImage procesarImagen(ByteBuffer buffer) throws IOException {
        return imageProcessor.fromBuffer(buffer);
    }
}