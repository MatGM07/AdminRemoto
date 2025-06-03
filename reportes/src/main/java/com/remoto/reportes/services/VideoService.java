package com.remoto.reportes.services;

import com.remoto.reportes.models.Video;
import com.remoto.reportes.models.VideoCacheMetadata;

import java.util.List;
import java.util.Optional;

public interface VideoService {
    Video guardar(Video video);
    Video obtenerPorId(Long id);
    List<Video> obtenerTodos();
    List<Video> obtenerPorSesionId(Long sesionId);
    Optional<VideoCacheMetadata> obtenerCacheMetadataPorSesionId(Long idSesion);
}
