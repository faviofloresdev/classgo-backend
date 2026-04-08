package com.classgo.backend.infrastructure.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageGateway {
    String upload(String key, MultipartFile file);
}
