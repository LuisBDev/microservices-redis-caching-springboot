package com.mspoc.users_service.service;

import com.mspoc.users_service.dto.request.CreateUserRequest;
import com.mspoc.users_service.dto.request.UpdateUserRequest;
import com.mspoc.users_service.dto.response.UserResponse;
import com.mspoc.users_service.entity.User;
import com.mspoc.users_service.exception.ResourceAlreadyExistsException;
import com.mspoc.users_service.exception.ResourceNotFoundException;
import com.mspoc.users_service.mapper.UserMapper;
import com.mspoc.users_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestión de usuarios.
 * <p>
 * Implementa la lógica de negocio para operaciones CRUD de usuarios.
 * Los usuarios no se cachean directamente, solo sus preferencias.
 *
 * @author Luis Balarezo
 */
@Service
public class UserService {

    public static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    /**
     * Crea un nuevo usuario.
     *
     * @param request Datos del usuario a crear
     * @return Usuario creado
     * @throws ResourceAlreadyExistsException Si ya existe un usuario con el email
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());

        // Validar que no exista el email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("User", "email", request.getEmail());
        }

        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);

        log.info("User created successfully with ID: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    /**
     * Obtiene un usuario por ID.
     *
     * @param id ID del usuario
     * @return Usuario encontrado
     * @throws ResourceNotFoundException Si no existe el usuario
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return userMapper.toResponse(user);
    }

    /**
     * Obtiene un usuario por ID con sus preferencias cargadas.
     * Usa FETCH JOIN para evitar N+1.
     *
     * @param id ID del usuario
     * @return Usuario con preferencias
     * @throws ResourceNotFoundException Si no existe el usuario
     */
    @Transactional(readOnly = true)
    public UserResponse getUserWithPreferences(Long id) {
        log.debug("Fetching user with preferences, ID: {}", id);

        User user = userRepository.findByIdWithPreferences(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return userMapper.toResponse(user);
    }

    /**
     * Obtiene un usuario por email.
     *
     * @param email Email del usuario
     * @return Usuario encontrado
     * @throws ResourceNotFoundException Si no existe el usuario
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.debug("Fetching user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new ResourceNotFoundException("User", "email", email);
                });

        return userMapper.toResponse(user);
    }

    /**
     * Obtiene todos los usuarios.
     *
     * @return Lista de usuarios
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");

        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    /**
     * Obtiene usuarios activos.
     *
     * @return Lista de usuarios activos
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getActiveUsers() {
        log.debug("Fetching active users");

        return userRepository.findByActive(true)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    /**
     * Busca usuarios por nombre.
     *
     * @param name Nombre a buscar
     * @return Lista de usuarios encontrados
     */
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsersByName(String name) {
        log.debug("Searching users with name: {}", name);

        return userRepository.searchByName(name)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    /**
     * Actualiza un usuario existente.
     *
     * @param id      ID del usuario
     * @param request Datos a actualizar
     * @return Usuario actualizado
     * @throws ResourceNotFoundException      Si no existe el usuario
     * @throws ResourceAlreadyExistsException Si el nuevo email ya existe
     */
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Si se está cambiando el email, validar que no exista
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ResourceAlreadyExistsException("User", "email", request.getEmail());
            }
        }

        userMapper.updateEntityFromRequest(request, user);
        User updatedUser = userRepository.save(user);

        log.info("User updated successfully with ID: {}", updatedUser.getId());
        return userMapper.toResponse(updatedUser);
    }

    /**
     * Elimina un usuario.
     *
     * @param id ID del usuario
     * @throws ResourceNotFoundException Si no existe el usuario
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
    }

    /**
     * Obtiene la entidad User (para uso interno).
     *
     * @param id ID del usuario
     * @return Entidad User
     * @throws ResourceNotFoundException Si no existe el usuario
     */
    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /**
     * Cuenta usuarios activos.
     *
     * @return Número de usuarios activos
     */
    @Transactional(readOnly = true)
    public Long countActiveUsers() {
        return userRepository.countActiveUsers();
    }
}
