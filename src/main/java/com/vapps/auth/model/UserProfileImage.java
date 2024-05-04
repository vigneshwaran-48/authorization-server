package com.vapps.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Document
@Data
public class UserProfileImage {
    
    @Id
	private String id;
	
	@DocumentReference
	private AppUser user;
	
	private String imageName;
	
	private String type;
	
    @Size(max = 100000)
	private byte[] imageBytes;
    
}
