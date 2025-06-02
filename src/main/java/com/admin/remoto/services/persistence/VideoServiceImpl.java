package com.admin.remoto.services.persistence;


import com.admin.remoto.Observador.Observable;
import com.admin.remoto.Observador.Observador;
import com.admin.remoto.models.Servidor;
import com.admin.remoto.models.Sesion;
import com.admin.remoto.models.Video;
import com.admin.remoto.repositories.VideoRepositorio;
import com.admin.remoto.services.business.SessionManager;
import com.admin.remoto.services.business.VideoEvento;
import com.admin.remoto.websocket.Evento;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VideoServiceImpl implements VideoService, Observable<Evento, Void> {

    private final VideoRepositorio videoRepository;
    private final List<Observador<Evento, Void>> observadores = new ArrayList<>();
    private final HttpServletRequest request;


    @Autowired
    public VideoServiceImpl(VideoRepositorio videoRepository, HttpServletRequest request) {
        this.request = request;
        this.videoRepository = videoRepository;
    }

    @Override
    public void agregarObservador(Observador<Evento, Void> obs) {
        observadores.add(obs);
    }

    @Override
    public void eliminarObservador(Observador<Evento, Void> obs) {
        observadores.remove(obs);
    }

    @Override
    public void notificarObservadores(Evento evento, Void contexto) {
        for (var obs : observadores) {
            obs.actualizar(evento, null);
        }
    }


    @Override
    @Transactional
    public Video saveVideo(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío o es null.");
        }

        // 1) Extraer metadatos
        String originalName = file.getOriginalFilename();
        long sizeBytes = file.getSize();
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String storedFileName = timestamp + "_" + originalName;

        // 2) Guardar en disco
        File uploadsDir = new File(System.getProperty("user.dir"), "videos");
        if (!uploadsDir.exists()) {
            boolean creada = uploadsDir.mkdirs();
            System.out.println(">>> [DEBUG] Carpeta 'videos' creada: " + creada);
        }

        File destino = new File(uploadsDir, storedFileName);
        try {
            file.transferTo(destino);
            System.out.println(">>> [DEBUG] Archivo transferido exitosamente a: " + destino.getAbsolutePath());
        } catch (IOException ioEx) {
            System.out.println(">>> [ERROR] Error al guardar en disco: " + ioEx.getMessage());
            throw ioEx;
        }
        byte[] data = file.getBytes();
        Video videoEntity = new Video(storedFileName, sizeBytes, now, data);

        // 3.1) Obtener la sesión activa desde SessionManager
        SessionManager sessionManager = SessionManager.getInstance();

        String ipCliente = request.getRemoteAddr();

// Buscar la sesión cuya dirección coincide (ignorando el puerto)
        Optional<Sesion> sesionCorrespondiente = sessionManager.getSesionesActivas().stream()
                .filter(s -> {
                    Servidor servidor = sessionManager.getServidorDeSesion(s);
                    return servidor != null && servidor.getDireccion().equals(ipCliente);
                })
                .findFirst();

        if (sesionCorrespondiente.isPresent()) {
            videoEntity.setSesion(sesionCorrespondiente.get());
        } else {
            System.out.println(">>> [WARN] No se encontró una sesión activa para la IP: " + ipCliente);
        }

        Video saved = videoRepository.save(videoEntity);

        notificarObservadores(new VideoEvento(saved.getId(), videoEntity.getSesion()), null);


        // 4) Notificar UI (en EDT de Swing)
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    null,
                    "Video recibido:\n" + storedFileName,
                    "Notificación",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        return saved;
    }
}
