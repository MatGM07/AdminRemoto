package com.remoto.reportes.services;

import com.remoto.reportes.models.Video;

import java.util.List;

public interface VideoService {
    Video guardar(Video video);
    Video obtenerPorId(Long id);
    List<Video> obtenerTodos();
    List<Video> obtenerPorSesionId(Long sesionId);
}
