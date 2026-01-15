package com.mspoc.users_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para User.
 * 
 * @author Luis Balarezo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Indicador de si tiene preferencias configuradas
    private Boolean hasPreferences;
}
