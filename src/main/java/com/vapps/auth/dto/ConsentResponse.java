package com.vapps.auth.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsentResponse {

    private int status;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    private ConsentDTO consentData;

}
