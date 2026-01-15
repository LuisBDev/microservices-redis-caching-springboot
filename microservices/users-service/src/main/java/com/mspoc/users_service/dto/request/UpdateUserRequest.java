package com.mspoc.users_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO para actualizar un usuario existente.
 * Todos los campos son opcionales para permitir actualizaciones parciales.
 *
 * @author Luis Balarezo
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone must be a valid international format")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    private Boolean active;
}
