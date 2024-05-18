package com.vapps.auth.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.lang.NonNull;

import com.vapps.auth.util.OAuth2Provider;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
    
    @NonNull 
	private String userName; 
	private String id;
	@NonNull 
	private String password;
	@DateTimeFormat(iso = ISO.DATE) 
	private LocalDate dob;
	private String profileImage;
	@NonNull
	private String email;
	private String mobile;
	private String firstName;	
	private String lastName;
	private int age;
	private OAuth2Provider provider;
}
