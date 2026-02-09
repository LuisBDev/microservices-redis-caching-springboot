package com.mspoc.notifications_service.controller;

import com.mspoc.notifications_service.dto.request.NotificationRequest;
import com.mspoc.notifications_service.dto.response.NotificationResponse;
import com.mspoc.notifications_service.service.impl.NotificationServiceImpl;
import com.mspoc.notifications_service.service.interfaces.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationRestController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Notifications Service is running!");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> sendNotification(@Valid @RequestBody NotificationRequest notificationRequest) {

        log.info("Request to send notification to user {} via {}", notificationRequest.getUserId(), notificationRequest.getChannel());

        NotificationResponse notificationResponse = notificationService.sendNotification(notificationRequest);


        return ResponseEntity.ok(notificationResponse);
    }

    @GetMapping("/can-send")
    public ResponseEntity<?> canSendNotification(
            @RequestParam Long userId,
            @RequestParam String channel) {

        boolean canSend = notificationService.canSendNotification(userId, channel);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("channel", channel);
        response.put("canSend", canSend);

        return ResponseEntity.ok(response);
    }
}
