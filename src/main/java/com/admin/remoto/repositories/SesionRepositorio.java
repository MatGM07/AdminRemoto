package com.admin.remoto.repositories;

import com.admin.remoto.models.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SesionRepositorio extends JpaRepository<Sesion, Long> {
    List<Sesion> findByUsuarioId(Long usuarioId);
}
