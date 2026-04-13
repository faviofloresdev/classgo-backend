package com.classgo.backend.shared.exception;

import com.classgo.backend.shared.dto.ApiErrorResponse;
import com.classgo.backend.shared.dto.ApiErrorResponse.ErrorBody;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import jakarta.validation.ConstraintViolationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleFrameworkNotFound(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Resource not found", request.getRequestURI());
    }

    @ExceptionHandler({UnauthorizedOperationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiErrorResponse> handleForbidden(RuntimeException ex, HttpServletRequest request) {
        HttpStatus status = ex instanceof BadCredentialsException ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
        String code = ex instanceof BadCredentialsException
            ? "UNAUTHORIZED"
            : ((UnauthorizedOperationException) ex).getCode();
        return build(status, code, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({BusinessRuleViolationException.class, DuplicateResourceException.class})
    public ResponseEntity<ApiErrorResponse> handleBusiness(RuntimeException ex, HttpServletRequest request) {
        String code = ex instanceof DuplicateResourceException duplicate
            ? duplicate.getCode()
            : ((BusinessRuleViolationException) ex).getCode();
        HttpStatus status = ex instanceof DuplicateResourceException ? HttpStatus.CONFLICT : HttpStatus.UNPROCESSABLE_ENTITY;
        return build(status, code, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiErrorResponse> handleValidation(Exception ex, HttpServletRequest request) {
        String message = ex instanceof MethodArgumentNotValidException manve
            ? manve.getBindingResult().getFieldErrors().stream().findFirst().map(FieldError::getDefaultMessage).orElse("Validation failed")
            : ex.getMessage();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleFallback(Exception ex, HttpServletRequest request) {
        Throwable cause = unwrap(ex);

        if (cause instanceof ResourceNotFoundException notFound) {
            return build(HttpStatus.NOT_FOUND, notFound.getCode(), notFound.getMessage(), request.getRequestURI());
        }
        if (cause instanceof NoResourceFoundException || cause instanceof NoHandlerFoundException) {
            return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Resource not found", request.getRequestURI());
        }
        if (cause instanceof UnauthorizedOperationException forbidden) {
            return build(HttpStatus.FORBIDDEN, forbidden.getCode(), forbidden.getMessage(), request.getRequestURI());
        }
        if (cause instanceof BadCredentialsException badCredentials) {
            return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", badCredentials.getMessage(), request.getRequestURI());
        }
        if (cause instanceof DuplicateResourceException duplicate) {
            return build(HttpStatus.CONFLICT, duplicate.getCode(), duplicate.getMessage(), request.getRequestURI());
        }
        if (cause instanceof BusinessRuleViolationException business) {
            return build(HttpStatus.UNPROCESSABLE_ENTITY, business.getCode(), business.getMessage(), request.getRequestURI());
        }
        if (cause instanceof MethodArgumentNotValidException manve) {
            String message = manve.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Validation failed");
            return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request.getRequestURI());
        }
        if (cause instanceof ConstraintViolationException || cause instanceof IllegalArgumentException) {
            return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", cause.getMessage(), request.getRequestURI());
        }
        if (isClientDisconnect(cause)) {
            log.info("Client disconnected during {} {}: {}", request.getMethod(), request.getRequestURI(), cause.getMessage());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        String message = cause.getMessage() == null || cause.getMessage().isBlank()
            ? cause.getClass().getSimpleName()
            : cause.getMessage();
        log.error("Unhandled exception processing {} {}", request.getMethod(), request.getRequestURI(), cause);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", message, request.getRequestURI());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String code, String message, String path) {
        return ResponseEntity.status(status)
            .body(new ApiErrorResponse(Instant.now(), status.value(), path, new ErrorBody(code, message)));
    }

    private Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while (current instanceof InvocationTargetException || current instanceof UndeclaredThrowableException) {
            Throwable next = current instanceof InvocationTargetException invocation
                ? invocation.getTargetException()
                : ((UndeclaredThrowableException) current).getUndeclaredThrowable();
            if (next == null || next == current) {
                break;
            }
            current = next;
        }
        return current;
    }

    private boolean isClientDisconnect(Throwable throwable) {
        if (!(throwable instanceof IOException)) {
            return false;
        }
        String message = throwable.getMessage();
        if (message == null) {
            return false;
        }
        String normalized = message.toLowerCase();
        return normalized.contains("connection was aborted")
            || normalized.contains("broken pipe")
            || normalized.contains("connection reset by peer")
            || normalized.contains("forcibly closed");
    }
}
