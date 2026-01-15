package com.mspoc.notifications_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferencesResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Boolean emailNotificationsEnabled;
    private Boolean pushNotificationsEnabled;
    private Boolean smsNotificationsEnabled;
    private Boolean marketingEmailsEnabled;
    private Boolean securityAlertsEnabled;
    private Boolean productUpdatesEnabled;
    private String notificationFrequency;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    private String timezone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isInQuietHours;
    private Boolean canReceiveEmail;
    private Boolean canReceivePush;
    private Boolean canReceiveSms;
}
