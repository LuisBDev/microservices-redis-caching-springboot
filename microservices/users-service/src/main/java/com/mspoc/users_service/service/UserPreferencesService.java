package com.mspoc.users_service.service;

import com.mspoc.users_service.dto.request.UpdateUserPreferencesRequest;
import com.mspoc.users_service.dto.request.UserPreferencesRequest;
import com.mspoc.users_service.dto.response.UserPreferencesResponse;
import com.mspoc.users_service.entity.User;
import com.mspoc.users_service.entity.UserPreferences;
import com.mspoc.users_service.exception.ResourceAlreadyExistsException;
import com.mspoc.users_service.exception.ResourceNotFoundException;
import com.mspoc.users_service.mapper.UserPreferencesMapper;
import com.mspoc.users_service.repository.UserPreferencesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing user preferences.
 * Implements the Cache-Aside pattern using Redis.
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
     * Creates new preferences for a user.
     * <p>
     * {@code @CachePut} always executes the method and updates the cache with the result,
     * regardless of whether a value already exists. Used for creating or updating data.
     *
     * @param request the user preferences request containing userId and preference settings
     * @return the created user preferences response
     * @throws ResourceAlreadyExistsException if preferences already exist for the user
     * @throws ResourceNotFoundException      if the user is not found
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
     * Retrieves user preferences by user ID.
     * <p>
     * Implements Cache-Aside pattern: searches first in Redis cache, if not found
     * queries the database and caches the result.
     * <p>
     * {@code @Cacheable} searches first in the cache. If a value exists, it returns it without
     * executing the method; otherwise, it executes the method, saves the result in the
     * cache, and returns it. Used for read operations.
     *
     * @param userId the ID of the user
     * @return the user preferences response
     * @throws ResourceNotFoundException if preferences are not found for the user
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

    /**
     * Updates user preferences and refreshes the cache.
     * <p>
     * {@code @CachePut} ensures the cache is updated with the latest preference values
     * after a successful update operation.
     *
     * @param userId  the ID of the user whose preferences are being updated
     * @param request the updated user preferences request
     * @return the updated user preferences response
     * @throws ResourceNotFoundException if preferences are not found for the user
     */
    @Transactional
    @CachePut(value = "user-preferences", key = "#userId")
    public UserPreferencesResponse updatePreferences(Long userId, UpdateUserPreferencesRequest request) {
        log.info("Updating preferences for user ID: {}", userId);

        UserPreferences preferences = preferencesRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserPreferences", "userId", userId));

        preferencesMapper.updateEntityFromRequest(request, preferences);
        UserPreferences updatedPreferences = preferencesRepository.save(preferences);
        UserPreferencesResponse response = preferencesMapper.toResponse(updatedPreferences);

        log.info("Preferences updated and cache refreshed for user ID: {}", userId);
        return response;
    }

    /**
     * Updates user preferences and synchronizes both individual and list caches.
     * <p>
     * This method uses {@code @Caching} to perform multiple cache operations atomically:
     * <ul>
     *   <li>{@code @CachePut} - Updates the individual user preference cache entry
     *       with the key pattern "user-preferences::{userId}"</li>
     *   <li>{@code @CacheEvict} - Evicts the cached list of all preferences
     *       with the key "user-preferences::all"</li>
     * </ul>
     * <p>
     * <strong>Cache Strategy:</strong>
     * <ul>
     *   <li>The individual preference is immediately updated in cache to ensure
     *       consistency for single-user queries</li>
     *   <li>The "all preferences" list is evicted to prevent stale data. The next
     *       call to {@link #getAllPreferences()} will regenerate and cache the
     *       updated list</li>
     *   <li>This approach avoids the complexity of manually updating the list cache
     *       while maintaining eventual consistency</li>
     * </ul>
     *
     * @param userId  the ID of the user whose preferences are being updated
     * @param request the updated user preferences request
     * @return the updated user preferences response
     * @throws ResourceNotFoundException if preferences are not found for the user
     */
    @Transactional
    @Caching(
            put = {
                    @CachePut(value = "user-preferences", key = "#userId")
            },
            evict = {
                    @CacheEvict(value = "user-preferences", key = "'all'")
            }
    )
    public UserPreferencesResponse updatePreferencesV2(Long userId, UpdateUserPreferencesRequest request) {
        log.info("V2 - Updating preferences for user ID: {}", userId);

        UserPreferences preferences = preferencesRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserPreferences", "userId", userId));

        preferencesMapper.updateEntityFromRequest(request, preferences);
        UserPreferences updatedPreferences = preferencesRepository.save(preferences);
        UserPreferencesResponse response = preferencesMapper.toResponse(updatedPreferences);

        log.info("V2 - Preferences updated and cache refreshed for user ID: {}", userId);
        return response;
    }


    @Transactional
//    @CacheEvict(value = "user-preferences", key = "#userId")
    @Caching(
            evict = {
                    @CacheEvict(value = "user-preferences", key = "#userId"),
                    @CacheEvict(value = "user-preferences", key = "'all'")
            })
    public void deletePreferencesV2(Long userId) {
        log.info("V2 - Deleting preferences for user ID: {}", userId);

        if (!preferencesRepository.existsByUserId(userId)) {
            throw new ResourceNotFoundException("UserPreferences", "userId", userId);
        }

        preferencesRepository.deleteByUserId(userId);
        log.info("V2 - Preferences deleted and evicted from cache for user ID: {}", userId);
    }

    /**
     * Deletes user preferences and evicts them from cache.
     * <p>
     * {@code @CacheEvict} removes the cached entry for the specified user ID after deletion.
     *
     * @param userId the ID of the user whose preferences are being deleted
     * @throws ResourceNotFoundException if preferences are not found for the user
     */
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
    @Cacheable(value = "user-preferences", key = "'all'", unless = "#result == null || #result.isEmpty()")
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

    /**
     * Retrieves all users with push notifications enabled.
     *
     * @return list of user preferences with push notifications enabled
     */
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

    /**
     * Clears all entries from the user-preferences cache.
     * <p>
     * This is useful for cache invalidation when needed, such as during
     * administrative operations or testing.
     */
    @CacheEvict(value = "user-preferences", allEntries = true)
    public void clearAllCache() {
        log.warn("Clearing entire user-preferences cache");
    }

    @Transactional(readOnly = true)
    public boolean hasPreferences(Long userId) {
        return preferencesRepository.existsByUserId(userId);
    }
}
