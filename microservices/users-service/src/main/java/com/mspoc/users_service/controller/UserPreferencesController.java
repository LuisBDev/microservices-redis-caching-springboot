package com.mspoc.users_service.controller;

import com.mspoc.users_service.dto.request.UserPreferencesRequest;
import com.mspoc.users_service.dto.response.ApiResponse;
import com.mspoc.users_service.dto.response.UserPreferencesResponse;
import com.mspoc.users_service.service.UserPreferencesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gestión de preferencias de usuario.
 */
@RestController
@RequestMapping("/preferences")
@RequiredArgsConstructor
@Slf4j
public class UserPreferencesController {

    private final UserPreferencesService preferencesService;

    /**
     * Crea nuevas preferencias para un usuario.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserPreferencesResponse>> createPreferences(
            @Valid @RequestBody UserPreferencesRequest request) {
        log.info("REST: Creating preferences for user ID: {}", request.getUserId());

        UserPreferencesResponse preferences = preferencesService.createPreferences(request);
        ApiResponse<UserPreferencesResponse> response = ApiResponse.success(
                "Preferences created successfully",
                preferences);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene preferencias de un usuario por su ID.
     * Implementa Cache-Aside para optimizar rendimiento.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<UserPreferencesResponse>> getPreferencesByUserId(@PathVariable Long userId) {
        log.info("REST: Fetching preferences for user ID: {}", userId);

        UserPreferencesResponse preferences = preferencesService.getPreferencesByUserId(userId);
        ApiResponse<UserPreferencesResponse> response = ApiResponse.success(preferences);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene preferencias por ID de preferencias.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserPreferencesResponse>> getPreferencesById(@PathVariable Long id) {
        log.debug("REST: Fetching preferences with ID: {}", id);

        UserPreferencesResponse preferences = preferencesService.getPreferencesById(id);
        ApiResponse<UserPreferencesResponse> response = ApiResponse.success(preferences);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene todas las preferencias.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserPreferencesResponse>>> getAllPreferences() {
        log.debug("REST: Fetching all preferences");

        List<UserPreferencesResponse> preferences = preferencesService.getAllPreferences();
        ApiResponse<List<UserPreferencesResponse>> response = ApiResponse.success(preferences);

        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza preferencias de un usuario.
     */
    @PutMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<UserPreferencesResponse>> updatePreferences(
            @PathVariable Long userId,
            @Valid @RequestBody UserPreferencesRequest request) {
        log.info("REST: Updating preferences for user ID: {}", userId);

        // Asegurar que el userId del path coincida con el del body
        request.setUserId(userId);

        UserPreferencesResponse preferences = preferencesService.updatePreferences(userId, request);
        ApiResponse<UserPreferencesResponse> response = ApiResponse.success(
                "Preferences updated successfully",
                preferences);

        return ResponseEntity.ok(response);
    }

    /**
     * Elimina preferencias de un usuario.
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Void>> deletePreferences(@PathVariable Long userId) {
        log.info("REST: Deleting preferences for user ID: {}", userId);

        preferencesService.deletePreferences(userId);
        ApiResponse<Void> response = ApiResponse.success("Preferences deleted successfully", null);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene usuarios con notificaciones por email habilitadas.
     */
    @GetMapping("/email-enabled")
    public ResponseEntity<ApiResponse<List<UserPreferencesResponse>>> getUsersWithEmailNotifications() {
        log.debug("REST: Fetching users with email notifications enabled");

        List<UserPreferencesResponse> preferences = preferencesService.getUsersWithEmailNotifications();
        ApiResponse<List<UserPreferencesResponse>> response = ApiResponse.success(preferences);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene usuarios con notificaciones push habilitadas.
     */
    @GetMapping("/push-enabled")
    public ResponseEntity<ApiResponse<List<UserPreferencesResponse>>> getUsersWithPushNotifications() {
        log.debug("REST: Fetching users with push notifications enabled");

        List<UserPreferencesResponse> preferences = preferencesService.getUsersWithPushNotifications();
        ApiResponse<List<UserPreferencesResponse>> response = ApiResponse.success(preferences);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene usuarios con marketing habilitado.
     */
    @GetMapping("/marketing-enabled")
    public ResponseEntity<ApiResponse<List<UserPreferencesResponse>>> getUsersWithMarketingEnabled() {
        log.debug("REST: Fetching users with marketing emails enabled");

        List<UserPreferencesResponse> preferences = preferencesService.getUsersWithMarketingEnabled();
        ApiResponse<List<UserPreferencesResponse>> response = ApiResponse.success(preferences);

        return ResponseEntity.ok(response);
    }

    /**
     * Limpia todo el caché de preferencias.
     * <p>
     * DELETE /preferences/cache
     */
    @DeleteMapping("/cache")
    public ResponseEntity<ApiResponse<Void>> clearCache() {
        log.warn("REST: Clearing all preferences cache");

        preferencesService.clearAllCache();
        ApiResponse<Void> response = ApiResponse.success("Cache cleared successfully", null);

        return ResponseEntity.ok(response);
    }

    /**
     * Verifica si un usuario tiene preferencias configuradas.
     * <p>
     * GET /preferences/user/{userId}/exists
     */
    @GetMapping("/user/{userId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> hasPreferences(@PathVariable Long userId) {
        log.debug("REST: Checking if user ID {} has preferences", userId);

        boolean exists = preferencesService.hasPreferences(userId);
        ApiResponse<Boolean> response = ApiResponse.success(exists);

        return ResponseEntity.ok(response);
    }
}
