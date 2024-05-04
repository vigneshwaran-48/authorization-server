package com.vapps.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class Authority {
    
    @Id
	private String id;

	private String userId;
	
	@Indexed(unique = true)
	private String authority;
}
