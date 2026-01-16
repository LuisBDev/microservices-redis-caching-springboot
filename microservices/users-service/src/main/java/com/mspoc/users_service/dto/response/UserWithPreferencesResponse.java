package com.mspoc.users_service.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * @author Luis Balarezo
 **/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserWithPreferencesResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserPreferencesResponse preferences;


}
