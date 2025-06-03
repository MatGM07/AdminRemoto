package com.admin.remoto.services.persistence;


import com.admin.remoto.Observador.Observable;
import com.admin.remoto.Observador.Observador;
import com.admin.remoto.models.Servidor;
import com.admin.remoto.models.Sesion;
import com.admin.remoto.models.Video;
import com.admin.remoto.models.VideoCacheMetadata;
import com.admin.remoto.repositories.VideoRepositorio;
import com.admin.remoto.services.business.SessionManager;
import com.admin.remoto.services.business.VideoEvento;
import com.admin.remoto.services.connection.Evento;
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
import java.util.Set;
import javax.sql.DataSource;

@Service
public class VideoServiceImpl implements VideoService, Observable<Evento, Void> {

    private final VideoRepositorio videoRepository;
    private final List<Observador<Evento, Void>> observadores = new ArrayList<>();
    private final HttpServletRequest request;
    private final SessionManager sessionManager;
    private final DataSource dataSource;
    private final VideoCacheMetadataRepository metadataRepository;

    @Autowired
    public VideoServiceImpl(VideoRepositorio videoRepository, HttpServletRequest request, SessionManager sessionManager, DataSource dataSource, VideoCacheMetadataRepository videoCacheMetadataRepository) {
        this.request = request;
        this.videoRepository = videoRepository;
        this.sessionManager = sessionManager;
        this.dataSource = dataSource;
        this.metadataRepository = videoCacheMetadataRepository;
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
        validarArchivo(file);

        String originalName = file.getOriginalFilename();
        long sizeBytes = file.getSize();
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String storedFileName = timestamp + "_" + originalName;

        byte[] data = file.getBytes();

        File uploadsDir = new File(System.getProperty("user.dir"), "videos");
        if (!uploadsDir.exists()) {
            boolean creada = uploadsDir.mkdirs();
            System.out.println(">>> [DEBUG] Carpeta 'videos' creada: " + creada);
        }

        File destino = new File(uploadsDir, storedFileName);
        try {
            file.transferTo(destino);  // transferir el archivo (luego de getBytes())
            System.out.println(">>> [DEBUG] Archivo transferido exitosamente a: " + destino.getAbsolutePath());
        } catch (IOException ioEx) {
            System.out.println(">>> [ERROR] Error al guardar en disco: " + ioEx.getMessage());
            throw ioEx;
        }

        Video videoEntity = new Video(storedFileName, sizeBytes, now, data);

        String ipCliente = request.getRemoteAddr();

        System.out.println(">>> [DEBUG] IP del cliente que sube el video: " + ipCliente);

        Set<Sesion> sesionesActivas = sessionManager.getSesionesActivas();
        System.out.println(">>> [DEBUG] Número de sesiones activas: " + sesionesActivas.size());

        for (Sesion s : sesionesActivas) {
            Servidor servidor = sessionManager.getServidorDeSesion(s);
            System.out.println(">>> [DEBUG] Sesión ID: " + s.getId()
                    + " | Servidor asociado: " + (servidor != null ? servidor.getDireccion() : "null"));
        }

        String ipClienteNormalizada = normalizarDireccion(ipCliente);

        Optional<Sesion> sesionCorrespondiente = sesionesActivas.stream()
                .filter(s -> {
                    Servidor servidor = sessionManager.getServidorDeSesion(s);
                    if (servidor == null) return false;

                    String direccionServidorNormalizada = normalizarDireccion(servidor.getDireccion());
                    return direccionServidorNormalizada.equals(ipClienteNormalizada);
                })
                .findFirst();

        if (sesionCorrespondiente.isPresent()) {
            System.out.println(">>> [DEBUG] Sesión encontrada para la IP: " + ipCliente
                    + " -> Sesión ID: " + sesionCorrespondiente.get().getId());
            videoEntity.setSesion(sesionCorrespondiente.get());
        } else {
            System.out.println(">>> [WARN] No se encontró una sesión activa para la IP: " + ipCliente);
        }

        System.out.println("sesion del vidio guardado: "+videoEntity.getSesion());
        Video saved = videoRepository.save(videoEntity);

        metadataRepository.save(new VideoCacheMetadata(
                videoEntity.getSizeBytes(),
                videoEntity.getUploadTime(),
                videoEntity.getSesion() != null ? videoEntity.getSesion().getId() : null
        ));

        notificarObservadores(new VideoEvento(saved.getId(), videoEntity.getSesion()), null);

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

    private String normalizarDireccion(String direccion) {
        if (direccion == null) return "";
        if (direccion.equalsIgnoreCase("localhost")) return "127.0.0.1";
        return direccion;
    }

    private void validarArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            System.out.println(">>> [WARN] Validación fallida: archivo vacío o null");
            throw new IllegalArgumentException("El archivo está vacío o es null.");
        }
    }
}
