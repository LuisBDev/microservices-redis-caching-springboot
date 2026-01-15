package com.mspoc.users_service.repository;

import com.mspoc.users_service.entity.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad UserPreferences.
 * 
 * @author Luis Balarezo
 */
@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {

    /**
     * Busca preferencias por ID de usuario.
     * 
     * @param userId ID del usuario
     * @return Optional con las preferencias si existen
     */
    Optional<UserPreferences> findByUserId(Long userId);

    /**
     * Verifica si existen preferencias para un usuario.
     * 
     * @param userId ID del usuario
     * @return true si existen preferencias
     */
    boolean existsByUserId(Long userId);

    /**
     * Elimina preferencias por ID de usuario.
     * 
     * @param userId ID del usuario
     */
    void deleteByUserId(Long userId);

    /**
     * Busca usuarios que aceptan notificaciones por email.
     * Útil para envíos masivos de email.
     * 
     * @return Lista de preferencias
     */
    @Query("SELECT p FROM UserPreferences p WHERE p.emailNotificationsEnabled = true AND p.user.active = true")
    List<UserPreferences> findUsersWithEmailNotificationsEnabled();

    /**
     * Busca usuarios que aceptan notificaciones push.
     * Útil para envíos masivos de push.
     * 
     * @return Lista de preferencias
     */
    @Query("SELECT p FROM UserPreferences p WHERE p.pushNotificationsEnabled = true AND p.user.active = true")
    List<UserPreferences> findUsersWithPushNotificationsEnabled();

    /**
     * Busca usuarios que aceptan notificaciones SMS.
     * 
     * @return Lista de preferencias
     */
    @Query("SELECT p FROM UserPreferences p WHERE p.smsNotificationsEnabled = true AND p.user.active = true")
    List<UserPreferences> findUsersWithSmsNotificationsEnabled();

    /**
     * Busca usuarios que aceptan emails de marketing.
     * 
     * @return Lista de preferencias
     */
    @Query("SELECT p FROM UserPreferences p WHERE p.marketingEmailsEnabled = true " +
            "AND p.emailNotificationsEnabled = true AND p.user.active = true")
    List<UserPreferences> findUsersWithMarketingEnabled();

    /**
     * Busca preferencias de usuario con el usuario cargado (FETCH JOIN).
     * Evita lazy loading.
     * 
     * @param userId ID del usuario
     * @return Optional con las preferencias y el usuario
     */
    @Query("SELECT p FROM UserPreferences p JOIN FETCH p.user WHERE p.user.id = :userId")
    Optional<UserPreferences> findByUserIdWithUser(@Param("userId") Long userId);

    /**
     * Cuenta usuarios con cada tipo de notificación habilitada.
     * Útil para estadísticas.
     */
    @Query("SELECT " +
            "SUM(CASE WHEN p.emailNotificationsEnabled = true THEN 1 ELSE 0 END) as emailCount, " +
            "SUM(CASE WHEN p.pushNotificationsEnabled = true THEN 1 ELSE 0 END) as pushCount, " +
            "SUM(CASE WHEN p.smsNotificationsEnabled = true THEN 1 ELSE 0 END) as smsCount " +
            "FROM UserPreferences p WHERE p.user.active = true")
    Object[] getNotificationStatistics();
}
