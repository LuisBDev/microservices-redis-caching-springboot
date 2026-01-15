package com.mspoc.users_service.controller;

import com.mspoc.users_service.dto.request.CreateUserRequest;
import com.mspoc.users_service.dto.request.UpdateUserRequest;
import com.mspoc.users_service.dto.response.ApiResponse;
import com.mspoc.users_service.dto.response.UserResponse;
import com.mspoc.users_service.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gestión de usuarios.
 *
 * @author Luis Balarezo
 */
@RestController
@RequestMapping("/users")
public class UserRestController {

    public static final Logger log = LoggerFactory.getLogger(UserRestController.class);

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/test/{id}")
    public ResponseEntity<String> testEndpoint(@PathVariable String id) {
        log.info("Test endpoint called with ID: {}", id);
        return ResponseEntity.ok("Test endpoint reached with ID: " + id);
    }


    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("REST: Creating user with email: {}", request.getEmail());

        UserResponse user = userService.createUser(request);
        ApiResponse<UserResponse> response = ApiResponse.success("User created successfully", user);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        log.debug("REST: Fetching user with ID: {}", id);

        UserResponse user = userService.getUserById(id);
        ApiResponse<UserResponse> response = ApiResponse.success(user);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un usuario por ID con sus preferencias cargadas.
     * <p>
     * GET /users/{id}/with-preferences
     */
    @GetMapping("/{id}/with-preferences")
    public ResponseEntity<ApiResponse<UserResponse>> getUserWithPreferences(@PathVariable Long id) {
        log.debug("REST: Fetching user with preferences, ID: {}", id);

        UserResponse user = userService.getUserWithPreferences(id);
        ApiResponse<UserResponse> response = ApiResponse.success(user);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        log.debug("REST: Fetching user with email: {}", email);

        UserResponse user = userService.getUserByEmail(email);
        ApiResponse<UserResponse> response = ApiResponse.success(user);

        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        log.debug("REST: Fetching all users");

        List<UserResponse> users = userService.getAllUsers();
        ApiResponse<List<UserResponse>> response = ApiResponse.success(users);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getActiveUsers() {
        log.debug("REST: Fetching active users");

        List<UserResponse> users = userService.getActiveUsers();
        ApiResponse<List<UserResponse>> response = ApiResponse.success(users);

        return ResponseEntity.ok(response);
    }

    /**
     * Busca usuarios por nombre.
     * <p>
     * GET /users/search?name={name}
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(@RequestParam String name) {
        log.debug("REST: Searching users with name: {}", name);

        List<UserResponse> users = userService.searchUsersByName(name);
        ApiResponse<List<UserResponse>> response = ApiResponse.success(users);

        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("REST: Updating user with ID: {}", id);

        UserResponse user = userService.updateUser(id, request);
        ApiResponse<UserResponse> response = ApiResponse.success("User updated successfully", user);

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("REST: Deleting user with ID: {}", id);

        userService.deleteUser(id);
        ApiResponse<Void> response = ApiResponse.success("User deleted successfully", null);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/stats/active-count")
    public ResponseEntity<ApiResponse<Long>> countActiveUsers() {
        log.debug("REST: Counting active users");

        Long count = userService.countActiveUsers();
        ApiResponse<Long> response = ApiResponse.success(count);

        return ResponseEntity.ok(response);
    }
}
