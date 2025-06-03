package com.admin.remoto.services.persistence;

import com.admin.remoto.models.VideoCacheMetadata;

public interface VideoCacheMetadataRepository {
    void save(VideoCacheMetadata metadata);
}
