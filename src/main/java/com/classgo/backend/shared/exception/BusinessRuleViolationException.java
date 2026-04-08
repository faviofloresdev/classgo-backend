package com.classgo.backend.shared.exception;

public class BusinessRuleViolationException extends RuntimeException {
    private final String code;

    public BusinessRuleViolationException(String message) {
        this("BUSINESS_RULE_VIOLATION", message);
    }

    public BusinessRuleViolationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
