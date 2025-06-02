package com.admin.remoto.repositories;

import com.admin.remoto.models.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepositorio extends JpaRepository<Video, Long> {
    // No necesita métodos adicionales por el momento
}
