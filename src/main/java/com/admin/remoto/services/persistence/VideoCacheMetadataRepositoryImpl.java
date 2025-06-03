package com.admin.remoto.services.persistence;

import com.admin.remoto.models.VideoCacheMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

@Repository
public class VideoCacheMetadataRepositoryImpl implements VideoCacheMetadataRepository {

    private final DataSource dataSource;

    @Autowired
    public VideoCacheMetadataRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(VideoCacheMetadata metadata) {
        String sql = "INSERT INTO video_cache_metadata (size_bytes, upload_time, sesion_id) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, metadata.getSizeBytes());
            stmt.setTimestamp(2, Timestamp.valueOf(metadata.getUploadTime()));
            if (metadata.getSesionId() != null) {
                stmt.setLong(3, metadata.getSesionId());
            } else {
                stmt.setNull(3, Types.BIGINT);
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(">>> [ERROR] Falló la inserción en video_cache_metadata: " + e.getMessage());
        }
    }
}
