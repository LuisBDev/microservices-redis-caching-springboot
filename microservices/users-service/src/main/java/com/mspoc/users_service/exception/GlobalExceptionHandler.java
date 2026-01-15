package com.mspoc.users_service.exception;

import com.mspoc.users_service.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Luis Balarezo
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja ResourceNotFoundException (404 Not Found).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource not found: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.NOT_FOUND);
        response.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Maneja ResourceAlreadyExistsException (409 Conflict).
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceAlreadyExistsException(
            ResourceAlreadyExistsException ex,
            HttpServletRequest request) {

        log.warn("Resource already exists: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.CONFLICT);
        response.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Maneja CacheException (500 Internal Server Error).
     */
    @ExceptionHandler(CacheException.class)
    public ResponseEntity<ApiResponse<Void>> handleCacheException(
            CacheException ex,
            HttpServletRequest request) {

        log.error("Cache error: {} - Path: {}", ex.getMessage(), request.getRequestURI(), ex);

        ApiResponse<Void> response = ApiResponse.error(
                "Cache service temporarily unavailable: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
        response.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Maneja errores de validación de Bean Validation (400 Bad Request).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.add(String.format("%s: %s", fieldName, errorMessage));
        });

        log.warn("Validation failed: {} - Path: {}", errors, request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                "Validation failed",
                errors,
                HttpStatus.BAD_REQUEST,
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja errores de tipo de argumento incorrecto (400 Bad Request).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String error = String.format("Parameter '%s' should be of type %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        log.warn("Type mismatch: {} - Path: {}", error, request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                error,
                HttpStatus.BAD_REQUEST);
        response.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja IllegalArgumentException (400 Bad Request).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Illegal argument: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST);
        response.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja todas las demás excepciones no capturadas (500 Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error: {} - Path: {}", ex.getMessage(), request.getRequestURI(), ex);

        ApiResponse<Void> response = ApiResponse.error(
                "An unexpected error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR);
        response.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
