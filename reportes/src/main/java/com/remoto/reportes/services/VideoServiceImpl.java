package com.remoto.reportes.services;

import com.remoto.reportes.models.Video;
import com.remoto.reportes.models.VideoCacheMetadata;
import com.remoto.reportes.repositories.VideoCacheMetadataRepository;
import com.remoto.reportes.repositories.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;
    private final VideoCacheMetadataRepository metadataRepository;

    @Autowired
    public VideoServiceImpl(VideoRepository videoRepository, VideoCacheMetadataRepository videoCacheMetadataRepository) {
        this.videoRepository = videoRepository;
        this.metadataRepository = videoCacheMetadataRepository;
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

    public Optional<VideoCacheMetadata> obtenerCacheMetadataPorSesionId(Long sesionId) {
        return metadataRepository.findLatestBySesionId(sesionId);
    }

    @Override
    public List<Video> obtenerPorSesionId(Long sesionId) {
        return videoRepository.findBySesionId(sesionId);
    }
}
