package com.vapps.auth.service;

import com.vapps.auth.dto.UserProfileImageDTO;
import com.vapps.auth.exception.AppException;

public interface UserProfileImageService {
    byte[] getImage(String userId, String imageName);

   void uploadImage(UserProfileImageDTO userImage) throws AppException;
}
