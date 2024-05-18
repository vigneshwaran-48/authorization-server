package com.vapps.auth.controller;

import java.security.Principal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vapps.auth.dto.UserControllerResponse;
import com.vapps.auth.dto.UserDTO;
import com.vapps.auth.dto.UserProfileImageDTO;
import com.vapps.auth.exception.AppException;
import com.vapps.auth.service.AppUserService;
import com.vapps.auth.service.UserProfileImageService;
import com.vapps.auth.util.OAuth2Provider;

@RestController
@RequestMapping(path = "/api/user")
@CrossOrigin("*")
public class UserController {

	private static final Logger LOG = LoggerFactory.getLogger(UserController.class);
	private static final String DESCRIPTION = "description";
	private static final String ERROR = "error";

	@Autowired
	private AppUserService appUserService;

	@Autowired
	private UserProfileImageService userProfileImageService;

	@PostMapping
	public ResponseEntity<UserControllerResponse> createUser(@RequestBody UserDTO requestUser) {
		try {
			requestUser.setProvider(OAuth2Provider.VAPPS);
			String userId = appUserService.createUser(requestUser);
			requestUser.setId(userId);
			requestUser.setPassword("*****");
		} catch (AppException e) {
			e.printStackTrace();
			Map<String, String> message = Map.of(ERROR, e.getMessage());
			return ResponseEntity.badRequest().body(new UserControllerResponse(message, null));
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			Map<String, String> message = Map.of(ERROR, e.getMessage());
			return ResponseEntity.internalServerError().body(new UserControllerResponse(message, null));
		}
		Map<String, String> message = Map.of(DESCRIPTION, "Created user");
		return new ResponseEntity<UserControllerResponse>(new UserControllerResponse(message, requestUser),
				HttpStatus.CREATED);
	}

	@GetMapping("me")
	public ResponseEntity<UserControllerResponse> me(Authentication principal) {
		if (principal == null || principal.getName() == null) {
			throw new AuthenticationCredentialsNotFoundException("Not authenticated");
		}
		UserDTO user = new UserDTO();
		try {
			LOG.info("Searching for user => " + principal.getName());
			user = appUserService.findByUserId(principal.getName());
			if (user == null) {
				throw new AppException("No user available with the current session details");
			}
			user.setPassword("*****");
		} catch (AuthenticationCredentialsNotFoundException e) {
			e.printStackTrace();
			Map<String, String> message = Map.of(ERROR, e.getMessage());
			return new ResponseEntity<UserControllerResponse>(new UserControllerResponse(message, null),
					HttpStatus.valueOf(403));
		} catch (AppException e) {
			LOG.error(e.getMessage(), e);
			Map<String, String> message = Map.of(ERROR, e.getMessage());
			return new ResponseEntity<UserControllerResponse>(new UserControllerResponse(message, null),
					HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			e.printStackTrace();
			Map<String, String> message = Map.of(ERROR, e.getMessage());
			return ResponseEntity.internalServerError().body(new UserControllerResponse(message, null));
		}
		Map<String, String> message = Map.of(DESCRIPTION, "success");
		return ResponseEntity.ok().body(new UserControllerResponse(message, user));
	}

	@GetMapping("{userId}/profile-image")
	public ResponseEntity<byte[]> getProfileImage(@PathVariable String userId) {
		byte[] profileImageBytes = null;
		try {
			profileImageBytes = userProfileImageService.getImage(userId);

			return ResponseEntity.ok().contentType(MediaType.valueOf("image/png")).body(profileImageBytes);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(null);
		}
	}

	@PutMapping("{id}/profile-image")
	public ResponseEntity<?> uploadImage(@PathVariable("id") String id,
			@RequestParam("profileImage") MultipartFile image, Principal principal) {
		try {
			if (principal == null || principal.getName() == null) {
				throw new AuthenticationCredentialsNotFoundException("Not authenticated");
			}
			if (image.isEmpty()) {
				throw new IllegalArgumentException("Empty file");
			}
			UserDTO userDetails = appUserService.findByUserId(id);
			LOG.info("Got user details for image uploading => " + userDetails);

			byte[] imageBytes = image.getBytes();

			UserProfileImageDTO userImage = new UserProfileImageDTO();
			userImage.setType(image.getContentType());
			LOG.info("Constructed profile image object => " + userImage);
			userImage.setImageBytes(imageBytes);

			userImage.setUserDetails(userDetails);

			userProfileImageService.uploadImage(userImage);
		} catch (AuthenticationCredentialsNotFoundException e) {
			LOG.error(e.getMessage(), e);
			Map<String, String> message = Map.of(ERROR, e.getMessage());
			return new ResponseEntity<UserControllerResponse>(new UserControllerResponse(message, null),
					HttpStatus.valueOf(403));
		} catch (IllegalArgumentException e) {
			LOG.error(e.getMessage(), e);
			return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
		}
		return new ResponseEntity<Map<String, String>>(Map.of("message", "success"), HttpStatus.CREATED);
	}

	@GetMapping("{id}")
	public ResponseEntity<UserControllerResponse> getUser(@PathVariable String id, Principal principal) {
		if (principal == null) {
			throw new AuthenticationCredentialsNotFoundException("Not authenticated");
		}
		UserDTO user = new UserDTO();
		try {
			user = appUserService.findByUserId(id);
			if (user == null) {
				throw new AppException(HttpStatus.BAD_REQUEST.value(), "No user available with the given id");
			}
			System.out.println(principal.getName() + ": " + user.getUserName());
			if (!principal.getName().equals(user.getUserName())) {
				throw new AppException(HttpStatus.FORBIDDEN.value(), "You don't have permissions to view this");
			}
			user.setPassword("*****");
		} catch (AuthenticationCredentialsNotFoundException e) {
			LOG.error(e.getMessage(), e);
			Map<String, String> message = Map.of(ERROR, e.getMessage());
			return new ResponseEntity<UserControllerResponse>(new UserControllerResponse(message, null),
					HttpStatus.valueOf(403));
		} catch (AppException e) {
			LOG.error(e.getMessage(), e);
			Map<String, String> message = Map.of(ERROR, e.getMessage());
			return new ResponseEntity<UserControllerResponse>(new UserControllerResponse(message, null),
					HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			Map<String, String> message = Map.of(ERROR, e.getMessage());
			return ResponseEntity.internalServerError().body(new UserControllerResponse(message, null));
		}
		Map<String, String> message = Map.of(DESCRIPTION, "success");
		return ResponseEntity.ok().body(new UserControllerResponse(message, user));
	}

	@PutMapping
	public ResponseEntity<?> updateUser(@RequestBody UserDTO UserDTO, Principal principal) {

		try {
			if (principal == null) {
				throw new AuthenticationCredentialsNotFoundException("Not authenticated");
			}
			UserDTO.setId(principal.getName());
			appUserService.updateUser(UserDTO);
		} catch (AppException e) {
			LOG.error(e.getMessage(), e);
			return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.NOT_FOUND);
		} catch (IllegalArgumentException e) {
			LOG.error(e.getMessage(), e);
			return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (AuthenticationCredentialsNotFoundException e) {
			LOG.error(e.getMessage(), e);
			Map<String, String> message = Map.of(ERROR, e.getMessage());
			return new ResponseEntity<UserControllerResponse>(new UserControllerResponse(message, null),
					HttpStatus.valueOf(403));
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return ResponseEntity.internalServerError().body(Map.of("message", "Oops! Something went wrong"));
		}
		return ResponseEntity.ok(Map.of("message", "success"));
	}

	@GetMapping("test")
	public void test() throws AppException {
		UserDTO appUser = new UserDTO();

		appUser.setFirstName("Vignesh");
		appUser.setLastName("warran");
		appUser.setUserName("vicky");
		appUser.setEmail("vicky.com");
		appUser.setPassword("vicky@123");

		appUserService.createUser(appUser);
	}
}
