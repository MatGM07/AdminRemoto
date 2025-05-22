package com.remoto.reportes.repositories;

import com.remoto.reportes.models.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SesionRepository extends JpaRepository<Sesion, Long> {
    List<Sesion> findByUsuarioId(Long usuarioId);
}