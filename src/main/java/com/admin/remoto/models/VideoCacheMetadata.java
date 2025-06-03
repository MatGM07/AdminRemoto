package com.admin.remoto.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_cache_metadata")
@org.hibernate.annotations.Immutable
public class VideoCacheMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;

    @Column(name = "sesion_id")
    private Long sesionId;

    public VideoCacheMetadata() {
    }

    public VideoCacheMetadata( Long sizeBytes, LocalDateTime uploadTime, Long sesionId) {
        this.sizeBytes = sizeBytes;
        this.uploadTime = uploadTime;
        this.sesionId = sesionId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public Long getSesionId() {
        return sesionId;
    }

    public void setSesionId(Long sesionId) {
        this.sesionId = sesionId;
    }

    public static class Builder {
        private Long sizeBytes;
        private LocalDateTime uploadTime;
        private Long sesionId;

        public Builder sizeBytes(Long sizeBytes) {
            this.sizeBytes = sizeBytes;
            return this;
        }

        public Builder uploadTime(LocalDateTime uploadTime) {
            this.uploadTime = uploadTime;
            return this;
        }

        public Builder sesionId(Long sesionId) {
            this.sesionId = sesionId;
            return this;
        }

        public VideoCacheMetadata build() {
            return new VideoCacheMetadata(sizeBytes, uploadTime, sesionId);
        }
    }
}
