package com.mspoc.notifications_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for {@link com.mspoc.notifications_service.entity.Notification}
 */
@Getter
@Setter
@Builder
public class NotificationRequest {

    @NotNull
    Long userId;

    @NotBlank
    String channel;

    @NotBlank
    String message;
    
}