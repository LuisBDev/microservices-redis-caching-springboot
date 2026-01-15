package com.mspoc.users_service.dto.request;

import com.mspoc.users_service.enums.NotificationFrequency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalTime;

/**
 * DTO para crear o actualizar preferencias de usuario.
 *
 * @author Luis Balarezo
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferencesRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @Builder.Default
    private Boolean emailNotificationsEnabled = true;

    @Builder.Default
    private Boolean pushNotificationsEnabled = true;

    @Builder.Default
    private Boolean smsNotificationsEnabled = false;

    @Builder.Default
    private Boolean marketingEmailsEnabled = false;

    @Builder.Default
    private Boolean securityAlertsEnabled = true;

    @Builder.Default
    private Boolean productUpdatesEnabled = true;

    @Builder.Default
    private NotificationFrequency notificationFrequency = NotificationFrequency.INSTANT;

    private LocalTime quietHoursStart;

    private LocalTime quietHoursEnd;

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    @Builder.Default
    private String timezone = "UTC";
}
