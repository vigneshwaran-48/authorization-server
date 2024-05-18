package com.vapps.auth.service;

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
import com.vapps.auth.util.OAuth2Provider;

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
		LOGGER.info("Loaded user {}", oauth2User);
		try {
			handleOAuth2User(oauth2User, userRequest.getClientRegistration().getRegistrationId());
		} catch (AppException e) {
			LOGGER.error(e.getMessage(), e);
			throw new OAuth2AuthenticationException("Error while handling oauth user");
		}

		return oauth2User;
	}

	private void handleOAuth2User(OAuth2User user, String providerId) throws AppException {

		String id = user.getName();
		LOGGER.info("User id {}", id);

		if(userRepository == null) {
            LOGGER.info("REPOSITORY IS NULL --------------");
			return;
		}
		if(userRepository.findById(id).isEmpty()) {
			OAuth2Provider oauth2Provider = OAuth2Provider.getByProviderName(providerId);
			if (oauth2Provider == null) {
				throw new AppException("Invalid provider!");
			}
			AppUser appUser = oauth2Provider.getProviderService(user).buildAppUser();

			userRepository.save(appUser);

            UserProvider userProvider = new UserProvider();
            userProvider.setProviderId(providerId);
            userProvider.setUser(appUser);
            userProviderRepository.save(userProvider);

            LOGGER.info("Created user from social login");

		}
		else {
            LOGGER.info("User with {} already present!", id);
		}
	}

}
