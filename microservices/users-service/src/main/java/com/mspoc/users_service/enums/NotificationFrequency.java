package com.mspoc.users_service.enums;

import lombok.Getter;

/**
 * Enum para frecuencia de notificaciones.
 */
@Getter
public enum NotificationFrequency {
    INSTANT("Instantáneo - envío inmediato"),
    HOURLY("Cada hora - agrupadas"),
    DAILY("Diario - resumen diario"),
    WEEKLY("Semanal - resumen semanal");

    private final String description;

    NotificationFrequency(String description) {
        this.description = description;
    }
}