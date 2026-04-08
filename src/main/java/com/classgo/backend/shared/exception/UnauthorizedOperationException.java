package com.classgo.backend.shared.exception;

public class UnauthorizedOperationException extends RuntimeException {
    private final String code;

    public UnauthorizedOperationException(String message) {
        this("UNAUTHORIZED_OPERATION", message);
    }

    public UnauthorizedOperationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
