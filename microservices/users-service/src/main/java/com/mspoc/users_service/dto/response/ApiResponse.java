package com.mspoc.users_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta estándar para APIs.
 * Proporciona una estructura consistente para todas las respuestas.
 *
 * @author Luis Balarezo
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private Boolean success;
    private String message;
    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private String path;
    private Integer statusCode;
    private List<String> errors;

    /**
     * Respuesta exitosa con datos.
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Respuesta exitosa con mensaje personalizado.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Respuesta de error.
     */
    public static <T> ApiResponse<T> error(String message, HttpStatus status) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .statusCode(status.value())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Respuesta de error con detalles.
     */
    public static <T> ApiResponse<T> error(String message, List<String> errors, HttpStatus status, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .statusCode(status.value())
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
