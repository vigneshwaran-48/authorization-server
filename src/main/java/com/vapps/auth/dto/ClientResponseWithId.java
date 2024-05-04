package com.vapps.auth.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponseWithId {

    private int status;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    private String clientId;

}
