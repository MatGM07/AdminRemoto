package com.remoto.reportes.repositories;

import com.remoto.reportes.models.VideoCacheMetadata;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class VideoCacheMetadataRepository {
    private final DataSource dataSource;

    public VideoCacheMetadataRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<VideoCacheMetadata> findLatestBySesionId(Long sesionId) {
        String sql = "SELECT id, size_bytes, upload_time, sesion_id " +
                "FROM video_cache_metadata " +
                "WHERE sesion_id = ? " +
                "ORDER BY upload_time DESC " +
                "LIMIT 1";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, sesionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    VideoCacheMetadata metadata = new VideoCacheMetadata();
                    metadata.setId(rs.getLong("id"));
                    metadata.setSizeBytes(rs.getLong("size_bytes"));
                    metadata.setUploadTime(rs.getTimestamp("upload_time").toLocalDateTime());
                    metadata.setSesionId(rs.getLong("sesion_id"));
                    return Optional.of(metadata);
                }
            }
        } catch (SQLException e) {
            System.err.println(">>> [ERROR] Falló la consulta a video_cache_metadata: " + e.getMessage());
        }

        return Optional.empty();
    }
}
