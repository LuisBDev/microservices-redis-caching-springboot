package com.mspoc.notifications_service.client;

import com.mspoc.notifications_service.client.dto.ApiResponse;
import com.mspoc.notifications_service.client.dto.UserPreferencesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "users-service")
public interface UsersServiceClient {

    @GetMapping("/preferences/user/{userId}")
    ApiResponse<UserPreferencesResponse> getUserPreferences(@PathVariable("userId") Long userId);
}
