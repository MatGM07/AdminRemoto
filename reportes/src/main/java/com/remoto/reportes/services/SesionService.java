package com.remoto.reportes.services;

import com.remoto.reportes.models.Sesion;

import java.util.List;
import java.util.Optional;

public interface SesionService {
    List<Sesion> obtenerTodas();
    Optional<Sesion> obtenerPorId(Long id);
    List<Sesion> obtenerPorUsuarioId(Long usuarioId);
    Sesion actualizar(Long id, Sesion sesionActualizada);
    Sesion guardar(Sesion sesion);
    void eliminar(Long id);
}

