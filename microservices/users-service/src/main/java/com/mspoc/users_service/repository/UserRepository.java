package com.mspoc.users_service.repository;

import com.mspoc.users_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad User.
 * 
 * @author Luis Balarezo
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por email.
     * 
     * @param email Email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el email dado.
     * 
     * @param email Email a verificar
     * @return true si existe
     */
    boolean existsByEmail(String email);

    /**
     * Busca usuarios por estado activo.
     * 
     * @param active Estado activo
     * @return Lista de usuarios
     */
    List<User> findByActive(Boolean active);

    /**
     * Busca usuarios activos que tengan preferencias configuradas.
     * Útil para obtener usuarios elegibles para notificaciones.
     * 
     * @return Lista de usuarios activos con preferencias
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.preferences WHERE u.active = true AND u.preferences IS NOT NULL")
    List<User> findActiveUsersWithPreferences();

    /**
     * Busca un usuario por ID con sus preferencias cargadas (FETCH JOIN).
     * Evita el problema N+1 al cargar la relación en una sola query.
     * 
     * @param id ID del usuario
     * @return Optional con el usuario y sus preferencias
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.preferences WHERE u.id = :id")
    Optional<User> findByIdWithPreferences(@Param("id") Long id);

    /**
     * Busca usuarios por nombre o apellido (búsqueda parcial, case-insensitive).
     * 
     * @param name Nombre a buscar
     * @return Lista de usuarios
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> searchByName(@Param("name") String name);

    /**
     * Cuenta usuarios activos.
     * 
     * @return Número de usuarios activos
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true")
    Long countActiveUsers();
}
