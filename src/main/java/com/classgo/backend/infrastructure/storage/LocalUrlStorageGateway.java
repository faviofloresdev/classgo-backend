package com.classgo.backend.infrastructure.storage;

import com.classgo.backend.infrastructure.config.AppProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class LocalUrlStorageGateway implements StorageGateway {

    private final AppProperties appProperties;

    public LocalUrlStorageGateway(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public String upload(String key, MultipartFile file) {
        return appProperties.storage().publicBaseUrl() + "/" + key;
    }
}
