package com.vapps.auth.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.vapps.auth.dto.UserDTO;
import com.vapps.auth.util.OAuth2Provider;

import lombok.Data;

@Document
@Data
public class AppUser {
    
    @Id
	private String id;	
	private String userName;
	@Indexed(unique = true)
	private String email;
	private int age;
	private LocalDate dob = LocalDate.of(1983, 3, 23);
	private String mobile;
	private String password;
	private String firstName;
	private String lastName;
	private String profileImage;
    private OAuth2Provider provider;

    public static AppUser build(UserDTO userDTO) {
        AppUser user = new AppUser();
        user.setId(userDTO.getId());
        user.setAge(userDTO.getAge());
        user.setDob(userDTO.getDob());
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setMobile(userDTO.getMobile());
        user.setPassword(userDTO.getPassword());
        user.setProfileImage(userDTO.getProfileImage());
        user.setUserName(userDTO.getUserName());
        user.setProvider(userDTO.getProvider());
        return user;
    }

    public UserDTO toDTO() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(id);
        userDTO.setAge(age);
        userDTO.setDob(dob);
        userDTO.setEmail(email);
        userDTO.setFirstName(firstName);
        userDTO.setLastName(lastName);
        userDTO.setMobile(mobile);
        userDTO.setPassword("*****");
        userDTO.setProfileImage(profileImage);
        userDTO.setUserName(userName);
        userDTO.setProvider(provider);
        return userDTO;
    }
}
