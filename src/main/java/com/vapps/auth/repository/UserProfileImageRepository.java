package com.vapps.auth.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vapps.auth.model.UserProfileImage;

public interface UserProfileImageRepository extends MongoRepository<UserProfileImage, String> {
 
    Optional<UserProfileImage> findByImageName(String imageName);
	
	Optional<UserProfileImage> findByImageNameAndUserId(String imageName, String userId);
}
