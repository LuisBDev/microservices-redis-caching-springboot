package com.mspoc.notifications_service.exception.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Luis Balarezo
 **/
@Getter
@Setter
@Builder
public class ErrorResponse {

    private String message;
    private List<String> errors;
    private LocalDateTime timestamp;
    private String path;
    private Integer statusCode;

}
