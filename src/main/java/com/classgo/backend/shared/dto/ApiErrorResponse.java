package com.classgo.backend.shared.dto;

import java.time.Instant;

public record ApiErrorResponse(
    Instant timestamp,
    int status,
    String path,
    ErrorBody error
) {
    public record ErrorBody(String code, String message) {
    }
}
