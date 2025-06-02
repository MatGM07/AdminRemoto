package com.remoto.reportes.repositories;

import com.remoto.reportes.models.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findBySesionId(Long sesionId);
}
