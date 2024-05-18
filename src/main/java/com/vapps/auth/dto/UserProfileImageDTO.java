package com.vapps.auth.dto;

import lombok.Data;

@Data
public class UserProfileImageDTO {
    
    private String id;
    private UserDTO userDetails;
    private String type;
    private byte[] imageBytes;

}
