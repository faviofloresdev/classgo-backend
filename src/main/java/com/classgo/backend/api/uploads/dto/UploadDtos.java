package com.classgo.backend.api.uploads.dto;

import java.util.UUID;

public final class UploadDtos {
    private UploadDtos() {}

    public record UploadResponse(UUID assetId, String publicUrl) {}
}
