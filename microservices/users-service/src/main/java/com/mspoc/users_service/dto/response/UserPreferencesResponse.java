package com.mspoc.users_service.dto.response;

import com.mspoc.users_service.enums.NotificationFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO de respuesta para UserPreferences.
 * <p>
 * Esta clase se serializa y almacena en Redis para el patrón Cache-Aside.
 * Por eso implementa Serializable.
 *
 * @author Luis Balarezo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferencesResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;

    // Canales de notificación
    private Boolean emailNotificationsEnabled;
    private Boolean pushNotificationsEnabled;
    private Boolean smsNotificationsEnabled;

    // Tipos de notificaciones
    private Boolean marketingEmailsEnabled;
    private Boolean securityAlertsEnabled;
    private Boolean productUpdatesEnabled;

    // Frecuencia y horarios
    private NotificationFrequency notificationFrequency;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    private String timezone;

    // Metadatos
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Flags de ayuda para el notification-service
    private Boolean isInQuietHours;
    private Boolean canReceiveEmail;
    private Boolean canReceivePush;
    private Boolean canReceiveSms;
}
