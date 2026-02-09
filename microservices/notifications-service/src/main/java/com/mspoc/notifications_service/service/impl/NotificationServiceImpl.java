package com.mspoc.notifications_service.service.impl;

import com.mspoc.notifications_service.client.UsersServiceClient;
import com.mspoc.notifications_service.client.dto.ApiResponse;
import com.mspoc.notifications_service.client.dto.UserPreferencesResponse;
import com.mspoc.notifications_service.dto.request.NotificationRequest;
import com.mspoc.notifications_service.dto.response.NotificationResponse;
import com.mspoc.notifications_service.entity.Notification;
import com.mspoc.notifications_service.exception.BusinessException;
import com.mspoc.notifications_service.repository.NotificationRepository;
import com.mspoc.notifications_service.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final UsersServiceClient usersServiceClient;
    private final NotificationRepository notificationRepository;

    @Override
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

    @Override
    public NotificationResponse sendNotification(NotificationRequest notificationRequest) {
        if (!canSendNotification(notificationRequest.getUserId(), notificationRequest.getChannel())) {
            throw new BusinessException("Cannot send notification: user preferences do not allow it", HttpStatus.FORBIDDEN);
        }
        
        notificationChannelSender(notificationRequest);

        Notification notificationEntity = Notification.builder()
                .userId(notificationRequest.getUserId())
                .channel(notificationRequest.getChannel())
                .message(notificationRequest.getMessage())
                .build();

        Notification savedEntity = notificationRepository.save(notificationEntity);

        return NotificationResponse.builder()
                .id(savedEntity.getId())
                .userId(savedEntity.getUserId())
                .channel(savedEntity.getChannel())
                .message(savedEntity.getMessage())
                .sentAt(savedEntity.getSentAt())
                .build();


    }

    private void notificationChannelSender(NotificationRequest notificationRequest) {
        log.info("Sending notification on channel {}  to user ID: {}", notificationRequest.getChannel(), notificationRequest.getUserId());
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
