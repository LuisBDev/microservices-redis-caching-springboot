package com.mspoc.notifications_service.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO for {@link com.mspoc.notifications_service.entity.Notification}
 */
@Getter
@Builder(toBuilder = true)
public class NotificationResponse {
    String id;
    Long userId;
    String channel;
    String message;
    LocalDateTime sentAt;
}