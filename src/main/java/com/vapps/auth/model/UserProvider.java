package com.vapps.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import lombok.Data;

@Document
@Data
public class UserProvider {
    
    @Id
	private String providerId;
	
	@DocumentReference
	private AppUser user;
}
