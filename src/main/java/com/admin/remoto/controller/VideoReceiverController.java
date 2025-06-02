package com.admin.remoto.controller;

import com.admin.remoto.models.Video;
import com.admin.remoto.services.persistence.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/upload")
public class VideoReceiverController {

    private final VideoService videoService;

    @Autowired
    public VideoReceiverController(VideoService videoService) {
        this.videoService = videoService;
    }

    /**
     * Recibe un MultipartFile, lo persiste en disco y en BD.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        System.out.println(">>> [DEBUG] Iniciando manejo de archivo");

        try {
            // 1) Persistir video en disco y BD
            Video saved = videoService.saveVideo(file);

            // 2) Responder con éxito y devolver el ID generado en la BD
            return ResponseEntity.ok("Video guardado con ID: " + saved.getId());
        } catch (IllegalArgumentException iae) {
            System.out.println(">>> [DEBUG] Archivo inválido: " + iae.getMessage());
            return ResponseEntity.badRequest().body(iae.getMessage());
        } catch (Exception e) {
            System.out.println(">>> [ERROR] Excepción al guardar el video:");
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al guardar archivo: " + e.getMessage());
        }
    }
}