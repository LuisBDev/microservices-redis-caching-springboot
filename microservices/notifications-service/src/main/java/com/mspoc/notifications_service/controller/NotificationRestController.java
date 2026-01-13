package com.mspoc.notifications_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * @author Luis Balarezo
 **/
@RestController
@RequestMapping("/notifications")
public class NotificationRestController {

    @GetMapping
    public ResponseEntity<?> test() {

        HashMap<String, Object> response = new HashMap<>();

        response.put("message", "Endpoint works!");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);

    }

}
