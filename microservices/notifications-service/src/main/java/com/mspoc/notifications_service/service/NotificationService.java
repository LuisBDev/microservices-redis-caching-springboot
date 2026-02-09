package com.mspoc.notifications_service.service;

import com.mspoc.notifications_service.client.UsersServiceClient;
import com.mspoc.notifications_service.client.dto.ApiResponse;
import com.mspoc.notifications_service.client.dto.UserPreferencesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final UsersServiceClient usersServiceClient;

    public boolean canSendNotification(Long userId, String channel) {
        try {
            log.debug("Checking notification preferences for user ID: {}, channel: {}", userId, channel);

            ApiResponse<UserPreferencesResponse> response = usersServiceClient.getUserPreferences(userId);

            if (response == null || !Boolean.TRUE.equals(response.getSuccess()) || response.getData() == null) {
                log.warn("Failed to fetch preferences for user ID: {}", userId);
                return false;
            }

            UserPreferencesResponse preferences = response.getData();

            if (Boolean.TRUE.equals(preferences.getIsInQuietHours())) {
                log.debug("User ID {} is in quiet hours, skipping notification", userId);
                return false;
            }

            return switch (channel.toUpperCase()) {
                case "EMAIL" -> Boolean.TRUE.equals(preferences.getCanReceiveEmail());
                case "PUSH" -> Boolean.TRUE.equals(preferences.getCanReceivePush());
                case "SMS" -> Boolean.TRUE.equals(preferences.getCanReceiveSms());
                default -> false;
            };

        } catch (Exception e) {
            log.error("Error checking notification preferences for user ID: {}", userId, e);
            return false;
        }
    }

    public void sendNotification(Long userId, String channel, String message) {
        if (!canSendNotification(userId, channel)) {
            log.info("Notification not sent to user ID {} via {}: preferences do not allow it", userId, channel);
            return;
        }

        log.info("Sending {} notification to user ID: {}", channel, userId);

        switch (channel.toUpperCase()) {
            case "EMAIL" -> sendEmail(userId, message);
            case "PUSH" -> sendPushNotification(userId, message);
            case "SMS" -> sendSms(userId, message);
            default -> log.warn("Unknown channel: {}", channel);
        }
    }

    private void sendEmail(Long userId, String message) {
        log.info("EMAIL sent to user {}: {}", userId, message);
    }

    private void sendPushNotification(Long userId, String message) {
        log.info("PUSH notification sent to user {}: {}", userId, message);
    }

    private void sendSms(Long userId, String message) {
        log.info("SMS sent to user {}: {}", userId, message);
    }
}
