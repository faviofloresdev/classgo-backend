package com.classgo.backend.shared.exception;

public class DuplicateResourceException extends RuntimeException {
    private final String code;

    public DuplicateResourceException(String message) {
        this("DUPLICATE_RESOURCE", message);
    }

    public DuplicateResourceException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
