package com.vapps.auth.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.vapps.auth.dto.UserProfileImageDTO;
import com.vapps.auth.exception.AppException;
import com.vapps.auth.model.AppUser;
import com.vapps.auth.model.UserProfileImage;
import com.vapps.auth.repository.UserProfileImageRepository;

@Service
public class UserProfileImageServiceImpl implements UserProfileImageService {

    @Autowired
    private UserProfileImageRepository userProfileImageRepository;

    public static final String PROFILE_IMAGE = "profile.png";
    private static final String DEFAULT_USER_ID = "-1";

    @Override
    public byte[] getImage(String userId) throws AppException {
        byte[] profileImageBytes = null;
        UserProfileImage profileImage = userProfileImageRepository.findByImageNameAndUserId(PROFILE_IMAGE, userId)
                .orElse(getDefaultUserImage());

        profileImageBytes = profileImage.getImageBytes();
        return profileImageBytes;
    }

    @Override
    public void uploadImage(UserProfileImageDTO userImage) throws AppException {
        Preconditions.checkArgument(userImage != null, "profile image details is null");
        Preconditions.checkArgument(userImage.getUserDetails() != null, "user details is null");

        UserProfileImage userProfileImage = getUserImage(userImage.getUserDetails().getId());

        if (userProfileImage == null) {
            userProfileImage = new UserProfileImage();
            userProfileImage.setUser(AppUser.build(userImage.getUserDetails()));
        }
        userProfileImage.setImageBytes(userImage.getImageBytes());
        userProfileImage.setImageName(PROFILE_IMAGE);
        userProfileImage.setType(userImage.getType());

        UserProfileImage uploadedImage = userProfileImageRepository.save(userProfileImage);
        if (uploadedImage == null) {
            throw new AppException("Error while uploading image to DB");
        }
    }

    private UserProfileImage getUserImage(String id) {
        return userProfileImageRepository.findByImageNameAndUserId(PROFILE_IMAGE, id).orElse(null);
    }

    @Override
    public Optional<UserProfileImageDTO> getDefaultImage() throws AppException {
        Optional<UserProfileImage> userProfileImage =
                userProfileImageRepository.findByImageNameAndUserId(PROFILE_IMAGE, DEFAULT_USER_ID);
        return userProfileImage.isEmpty() ? Optional.empty() : Optional.of(userProfileImage.get().toDTO());
    }

    public UserProfileImage getDefaultUserImage() throws AppException {
        return userProfileImageRepository.findByImageNameAndUserId(PROFILE_IMAGE, DEFAULT_USER_ID).orElseThrow();
    }
}
