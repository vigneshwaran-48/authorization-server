package com.vapps.auth.util.providers;

import com.vapps.auth.model.AppUser;

public class GoogleProviderService extends ProviderService {

    @Override
    public String getId() {
        return oAuth2User.getName();
    }

    @Override
    public AppUser buildAppUser() {
        AppUser userDetails = new AppUser();

		userDetails.setId(oAuth2User.getName());

        String name = oAuth2User.getAttribute("name");
        String email = oAuth2User.getAttribute("email");
        
		userDetails.setUserName(name);
		userDetails.setFirstName(name);
		userDetails.setLastName(name != null ? name.substring(0, 1) : "");
		userDetails.setEmail(email != null ? email : "");
        userDetails.setProfileImage(oAuth2User.getAttribute("picture"));
		setPasswordForExternalServiceUser(userDetails);

		return userDetails;
    }
    
}
