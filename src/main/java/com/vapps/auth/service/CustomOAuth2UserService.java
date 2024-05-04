package com.vapps.auth.service;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.vapps.auth.exception.AppException;
import com.vapps.auth.model.AppUser;
import com.vapps.auth.model.UserProvider;
import com.vapps.auth.repository.UserProviderRepository;
import com.vapps.auth.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
	private UserRepository userRepository;

    @Autowired
    private UserProviderRepository userProviderRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomOAuth2UserService.class);

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oauth2User = super.loadUser(userRequest);

		try {
			handleOAuth2User(oauth2User, userRequest.getClientRegistration().getRegistrationId());
		} catch (AppException e) {
			LOGGER.error(e.getMessage(), e);
			throw new OAuth2AuthenticationException("Error while handling oauth user");
		}

		return oauth2User;
	}

	private void handleOAuth2User(OAuth2User user, String providerId) throws AppException {
        LOGGER.info("Got OAuth2 user {}", user);

		String id = user.getAttributes().get("id").toString();

		if(userRepository == null) {
            LOGGER.info("REPOSITORY IS NULL --------------");
			return;
		}
		if(userRepository.findById(id).isEmpty()) {
			AppUser appUser = getUserFromOAuth2User(user.getAttributes());
            LOGGER.info("User to be added {}", appUser.getId());

			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			userRepository.save(appUser);

            UserProvider userProvider = new UserProvider();
            userProvider.setProviderId(providerId);
            userProvider.setUser(appUser);
            userProviderRepository.save(userProvider);

            LOGGER.info("Created user ................ from social login");

		}
		else {
            LOGGER.info("User with {} already present!", id);
		}
	}

	private AppUser getUserFromOAuth2User(Map<String, Object> attributes) {
		AppUser userDetails = new AppUser();

        //Handling only for github users ...
		userDetails.setId(attributes.get("id").toString());
		userDetails.setUserName(attributes.get("login").toString());
		userDetails.setFirstName(attributes.get("login").toString());
		userDetails.setLastName(attributes.get("login").toString().substring(0, 1));
		userDetails.setEmail(attributes.get("email") != null ? attributes.get("email").toString() : "");
		userDetails.setPassword("****(Other service account)");
		userDetails.setProfileImage(attributes.get("avatar_url").toString());

		return userDetails;
	}
    
}
