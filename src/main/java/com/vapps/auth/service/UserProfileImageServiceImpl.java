package com.vapps.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vapps.auth.dto.UserProfileImageDTO;
import com.vapps.auth.exception.AppException;
import com.vapps.auth.model.UserProfileImage;
import com.vapps.auth.repository.UserProfileImageRepository;

@Service
public class UserProfileImageServiceImpl implements UserProfileImageService {

    @Autowired
	private UserProfileImageRepository userProfileImageRepository;
    
    @Override
    public byte[] getImage(String userId, String imageName) {
        byte[] profileImageBytes = null;
		UserProfileImage profileImage = userProfileImageRepository
				.findByImageNameAndUserId(imageName, userId).orElse(getDefaultImage());
		
		profileImageBytes = profileImage.getImageBytes();
		return profileImageBytes;
    }

    @Override
    public void uploadImage(UserProfileImageDTO userImage) throws AppException {
        // TODO Auto-generated method stub
    }
    
    private UserProfileImage getDefaultImage() {
		UserProfileImage userProfileImage = userProfileImageRepository.findById("-1").orElseThrow();
		return userProfileImage;
	}
}
