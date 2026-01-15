package com.mspoc.users_service.service;

import com.mspoc.users_service.dto.request.UserPreferencesRequest;
import com.mspoc.users_service.dto.response.UserPreferencesResponse;
import com.mspoc.users_service.entity.User;
import com.mspoc.users_service.entity.UserPreferences;
import com.mspoc.users_service.exception.ResourceAlreadyExistsException;
import com.mspoc.users_service.exception.ResourceNotFoundException;
import com.mspoc.users_service.mapper.UserPreferencesMapper;
import com.mspoc.users_service.repository.UserPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestión de preferencias de usuario.
 * Implementa el patrón Cache-Aside usando Redis.
 */
@Service
@Slf4j
public class UserPreferencesService {

    private final UserPreferencesRepository preferencesRepository;
    private final UserPreferencesMapper preferencesMapper;
    private final UserService userService;

    public UserPreferencesService(UserPreferencesRepository preferencesRepository,
                                  UserPreferencesMapper preferencesMapper,
                                  UserService userService) {
        this.preferencesRepository = preferencesRepository;
        this.preferencesMapper = preferencesMapper;
        this.userService = userService;
    }

    /**
     * Crea nuevas preferencias para un usuario.
     */
    @Transactional
    @CachePut(value = "user-preferences", key = "#request.userId")
    public UserPreferencesResponse createPreferences(UserPreferencesRequest request) {
        log.info("Creating preferences for user ID: {}", request.getUserId());

        User user = userService.getUserEntityById(request.getUserId());
        if (preferencesRepository.existsByUserId(request.getUserId())) {
            throw new ResourceAlreadyExistsException("UserPreferences", "userId", request.getUserId());
        }

        UserPreferences preferences = preferencesMapper.toEntity(request);
        preferences.setUser(user);

        UserPreferences savedPreferences = preferencesRepository.save(preferences);
        UserPreferencesResponse response = preferencesMapper.toResponse(savedPreferences);

        log.info("Preferences created and cached for user ID: {}", request.getUserId());
        return response;
    }

    /**
     * Obtiene las preferencias de un usuario por su ID.
     * Implementa Cache-Aside: busca primero en Redis, si no existe consulta BD y
     * cachea.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "user-preferences", key = "#userId", unless = "#result == null")
    public UserPreferencesResponse getPreferencesByUserId(Long userId) {
        log.debug("Fetching preferences for user ID: {}", userId);

        UserPreferences preferences = preferencesRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserPreferences", "userId", userId));

        UserPreferencesResponse response = preferencesMapper.toResponse(preferences);

        log.debug("Preferences fetched from database for user ID: {}", userId);
        return response;
    }

    @Transactional(readOnly = true)
    public UserPreferencesResponse getPreferencesById(Long id) {
        log.debug("Fetching preferences with ID: {}", id);

        UserPreferences preferences = preferencesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserPreferences", "id", id));

        return preferencesMapper.toResponse(preferences);
    }

    @Transactional
    @CachePut(value = "user-preferences", key = "#userId")
    public UserPreferencesResponse updatePreferences(Long userId, UserPreferencesRequest request) {
        log.info("Updating preferences for user ID: {}", userId);

        UserPreferences preferences = preferencesRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserPreferences", "userId", userId));

        preferencesMapper.updateEntityFromRequest(request, preferences);
        UserPreferences updatedPreferences = preferencesRepository.save(preferences);
        UserPreferencesResponse response = preferencesMapper.toResponse(updatedPreferences);

        log.info("Preferences updated and cache refreshed for user ID: {}", userId);
        return response;
    }

    @Transactional
    @CacheEvict(value = "user-preferences", key = "#userId")
    public void deletePreferences(Long userId) {
        log.info("Deleting preferences for user ID: {}", userId);

        if (!preferencesRepository.existsByUserId(userId)) {
            throw new ResourceNotFoundException("UserPreferences", "userId", userId);
        }

        preferencesRepository.deleteByUserId(userId);
        log.info("Preferences deleted and evicted from cache for user ID: {}", userId);
    }

    @Transactional(readOnly = true)
    public List<UserPreferencesResponse> getAllPreferences() {
        log.debug("Fetching all preferences");

        return preferencesRepository.findAll()
                .stream()
                .map(preferencesMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserPreferencesResponse> getUsersWithEmailNotifications() {
        log.debug("Fetching users with email notifications enabled");

        return preferencesRepository.findUsersWithEmailNotificationsEnabled()
                .stream()
                .map(preferencesMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserPreferencesResponse> getUsersWithPushNotifications() {
        log.debug("Fetching users with push notifications enabled");

        return preferencesRepository.findUsersWithPushNotificationsEnabled()
                .stream()
                .map(preferencesMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserPreferencesResponse> getUsersWithMarketingEnabled() {
        log.debug("Fetching users with marketing emails enabled");

        return preferencesRepository.findUsersWithMarketingEnabled()
                .stream()
                .map(preferencesMapper::toResponse)
                .toList();
    }

    @CacheEvict(value = "user-preferences", allEntries = true)
    public void clearAllCache() {
        log.warn("Clearing entire user-preferences cache");
    }

    @Transactional(readOnly = true)
    public boolean hasPreferences(Long userId) {
        return preferencesRepository.existsByUserId(userId);
    }
}
