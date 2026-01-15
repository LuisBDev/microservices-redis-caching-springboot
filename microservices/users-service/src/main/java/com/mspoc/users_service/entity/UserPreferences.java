package com.mspoc.users_service.entity;

import com.mspoc.users_service.enums.NotificationFrequency;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidad UserPreferences - Preferencias de notificación del usuario.
 * <p>
 * Esta entidad almacena todas las preferencias relacionadas con notificaciones
 * y se cachea frecuentemente para reducir la carga en la base de datos durante
 * envíos masivos de notificaciones.
 * <p>
 * Implementa el patrón Cache-Aside para optimizar lecturas frecuentes.
 *
 * @author Luis Balarezo
 */
@Entity
@Table(name = "user_preferences", indexes = {
        @Index(name = "idx_user_preferences_user_id", columnList = "user_id"),
        @Index(name = "idx_user_preferences_email_enabled", columnList = "email_notifications_enabled"),
        @Index(name = "idx_user_preferences_push_enabled", columnList = "push_notifications_enabled")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user") // Evita lazy loading en toString
public class UserPreferences implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // Canales de Notificación
    @Column(name = "email_notifications_enabled", nullable = false)
    private Boolean emailNotificationsEnabled;

    @Column(name = "push_notifications_enabled", nullable = false)
    private Boolean pushNotificationsEnabled;

    @Column(name = "sms_notifications_enabled", nullable = false)
    private Boolean smsNotificationsEnabled;

    // Tipos de Notificaciones
    @Column(name = "marketing_emails_enabled", nullable = false)
    private Boolean marketingEmailsEnabled;

    @Column(name = "security_alerts_enabled", nullable = false)
    private Boolean securityAlertsEnabled;

    @Column(name = "product_updates_enabled", nullable = false)
    private Boolean productUpdatesEnabled;

    // Frecuencia y Horarios
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_frequency", nullable = false, length = 20)
    private NotificationFrequency notificationFrequency;

    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;

    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;

    @Column(nullable = false, length = 50)
    private String timezone;

    // Metadatos
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relación 1:1 con User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @NotNull(message = "User is required")
    private User user;


    /**
     * Verifica si el usuario acepta notificaciones por email.
     * Método de utilidad para el notification-service.
     */
    public boolean acceptsEmailNotifications() {
        return Boolean.TRUE.equals(emailNotificationsEnabled);
    }

    /**
     * Verifica si el usuario acepta notificaciones push.
     */
    public boolean acceptsPushNotifications() {
        return Boolean.TRUE.equals(pushNotificationsEnabled);
    }

    /**
     * Verifica si el usuario acepta notificaciones SMS.
     */
    public boolean acceptsSmsNotifications() {
        return Boolean.TRUE.equals(smsNotificationsEnabled);
    }

    /**
     * Verifica si actualmente está en horario de silencio.
     */
    public boolean isInQuietHours() {
        if (quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }

        LocalTime now = LocalTime.now();

        // Si el período cruza la medianoche
        if (quietHoursStart.isAfter(quietHoursEnd)) {
            return now.isAfter(quietHoursStart) || now.isBefore(quietHoursEnd);
        }

        // Período normal en el mismo día
        return now.isAfter(quietHoursStart) && now.isBefore(quietHoursEnd);
    }

    /**
     * Verifica si debe enviar la notificación basado en canales y horarios.
     */
    public boolean shouldSendNotification(String channel) {
        if (isInQuietHours()) {
            return false;
        }

        return switch (channel.toUpperCase()) {
            case "EMAIL" -> acceptsEmailNotifications();
            case "PUSH" -> acceptsPushNotifications();
            case "SMS" -> acceptsSmsNotifications();
            default -> false;
        };
    }
}
