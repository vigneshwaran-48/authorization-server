package com.vapps.auth.service;

import java.util.Optional;

import com.vapps.auth.dto.UserProfileImageDTO;
import com.vapps.auth.exception.AppException;

public interface UserProfileImageService {
    byte[] getImage(String userId) throws AppException;

   void uploadImage(UserProfileImageDTO userImage) throws AppException;

   Optional<UserProfileImageDTO> getDefaultImage() throws AppException;
}
