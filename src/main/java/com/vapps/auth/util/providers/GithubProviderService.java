package com.vapps.auth.util.providers;

import java.util.Map;

import com.vapps.auth.model.AppUser;

public class GithubProviderService extends ProviderService {

    @Override
    public String getId() {
        return oAuth2User.getName();
    }

    @Override
    public AppUser buildAppUser() {
        AppUser userDetails = new AppUser();

        Map<String, Object> attributes = super.oAuth2User.getAttributes();

		userDetails.setId(attributes.get("id").toString());
		userDetails.setUserName(attributes.get("login").toString());
		userDetails.setFirstName(attributes.get("login").toString());
		userDetails.setLastName(attributes.get("login").toString().substring(0, 1));
		userDetails.setEmail(attributes.get("email") != null ? attributes.get("email").toString() : "");
		setPasswordForExternalServiceUser(userDetails);
		userDetails.setProfileImage(attributes.get("avatar_url").toString());

		return userDetails;
    }
}