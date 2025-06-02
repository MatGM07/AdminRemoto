package com.remoto.reportes.services;

import com.remoto.reportes.models.Video;
import com.remoto.reportes.repositories.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;

    @Autowired
    public VideoServiceImpl(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @Override
    public Video guardar(Video video) {
        return videoRepository.save(video);
    }

    @Override
    public Video obtenerPorId(Long id) {
        Optional<Video> video = videoRepository.findById(id);
        return video.orElse(null);
    }

    @Override
    public List<Video> obtenerTodos() {
        return videoRepository.findAll();
    }

    @Override
    public List<Video> obtenerPorSesionId(Long sesionId) {
        return videoRepository.findBySesionId(sesionId);
    }
}
