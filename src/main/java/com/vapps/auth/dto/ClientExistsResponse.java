package com.vapps.auth.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClientExistsResponse {

    private int status;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    private boolean isClientExists;

}
