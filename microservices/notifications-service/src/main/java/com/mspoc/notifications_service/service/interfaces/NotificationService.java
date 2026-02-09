package com.mspoc.notifications_service.service.interfaces;

import com.mspoc.notifications_service.dto.request.NotificationRequest;
import com.mspoc.notifications_service.dto.response.NotificationResponse;

/**
 * @author Luis Balarezo
 **/
public interface NotificationService {

    boolean canSendNotification(Long userId, String channel);

    NotificationResponse sendNotification(NotificationRequest notificationRequest);
}
