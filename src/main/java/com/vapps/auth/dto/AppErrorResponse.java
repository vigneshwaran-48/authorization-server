package com.vapps.auth.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppErrorResponse {
    
    private int status;
    private String error;
    private LocalDateTime timestamp;
    private String path;

}
