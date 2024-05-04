package com.vapps.auth.dto;

import lombok.Data;

@Data
public class UserProfileImageDTO {
    
    private Long id;
    private UserDTO userDetails;
    private String imageName;
    private String type;
    private byte[] imageBytes;

}
