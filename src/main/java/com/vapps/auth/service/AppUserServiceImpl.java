package com.vapps.auth.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.vapps.auth.dto.UserDTO;
import com.vapps.auth.exception.AppException;
import com.vapps.auth.model.AppUser;
import com.vapps.auth.model.AppUserPrincipal;
import com.vapps.auth.repository.UserRepository;
import com.vapps.auth.util.OAuth2Provider;

@Service
public class AppUserServiceImpl implements UserDetailsManager, AppUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Value("${app.baseurl}")
    private String appbaseUrl;

    private static final Logger LOGGER = LoggerFactory.getLogger(AppUserServiceImpl.class);
    private static final String USERNOTFOUND = "User Not Found for ";
    private static final String CONTROLLER_ENDPOINT = "/api/user";

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = userRepository.findById(username).orElse(null);

        if (appUser == null) {
            appUser = userRepository.findByUserName(username);
            if (appUser == null) {
                appUser = userRepository.findByEmailAndProvider(username, OAuth2Provider.VAPPS)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            }
        }

        Collection<GrantedAuthority> authorities = new HashSet<>();

        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        String name = appUser.getId() != null ? appUser.getId().toString() : String.valueOf(-1);

        AppUserPrincipal user = new AppUserPrincipal();
        user.setUserName(name);
        user.setAuthorities(authorities);
        user.setPassword(appUser.getPassword());
        return user;
    }

    @Override
    public void createUser(UserDetails user) {
        AppUser appUser = toAppUser(user);
        userRepository.save(appUser);
    }

    @Override
    public void updateUser(UserDetails user) {
        AppUser appUser = toAppUser(user);
        userRepository.save(appUser);
    }

    @Override
    public void deleteUser(String username) {
        userRepository.deleteByUserName(username);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {

    }

    @Override
    public boolean userExists(String username) {
        Optional<AppUser> appUser = userRepository.findById(username);
        return appUser.isPresent();
    }

    private AppUser toAppUser(UserDetails user) {

        AppUser appUser = new AppUser();

        appUser.setUserName(user.getUsername());
        appUser.setPassword(user.getPassword());

        return appUser;
    }

    @Override
    public String createUser(UserDTO user) throws AppException {
        Preconditions.checkArgument(user != null, "User data can't be empty");
        AppUser appUser = AppUser.build(user);

        if (userRepository.findByUserNameAndProvider(user.getUserName(), user.getProvider()).isPresent()) {
            LOGGER.info("Name {} for provider {} already in use", user.getUserName(), user.getProvider());
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "That name already taken");
        }

        if (userRepository.findByEmailAndProvider(user.getEmail(), user.getProvider()).isPresent()) {
            LOGGER.info("Email {} for provider {} already in use", user.getUserName(), user.getProvider());
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Email exists");
        }

        if (appUser.getId() == null) {
            UUID uuid = UUID.randomUUID();
            appUser.setId(uuid.toString());
        }
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        if (appUser.getDob() == null) {
            appUser.setDob(LocalDate.now());
        }
        if (appUser.getProfileImage() == null || appUser.getProfileImage().isBlank()) {
            appUser.setProfileImage(appbaseUrl + "/api/user/" + appUser.getId() + "/profile-image");
        }
        AppUser createdUser = userRepository.save(appUser);

        if (createdUser == null) {
            throw new InternalError("Error in creating the user");
        }
        return createdUser.getId();
    }

    @Override
    public void updateUser(UserDTO user) throws AppException {
        Preconditions.checkArgument(user != null, "User data can't be empty");

        AppUser appUser = AppUser.build(user);
        UserDTO prevUserData = findByUserId(appUser.getId());

        if (prevUserData == null) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "User not found");
        }

        checkAndUpdateUser(AppUser.build(prevUserData), appUser);

        Optional<AppUser> userByName = userRepository.findByUserNameAndProvider(user.getUserName(), user.getProvider());
        if (userByName.isPresent() && !userByName.get().getId().equals(user.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "That name already taken!");
        }

        Optional<AppUser> userByEmail = userRepository.findByEmailAndProvider(user.getEmail(), user.getProvider());
        if (userByEmail.isPresent() && !userByEmail.get().getId().equals(user.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "That email already exists!");
        }

        userRepository.save(appUser);
    }

    @Override
    public UserDTO findByUserId(String id) throws AppException {
        Preconditions.checkArgument(id != null && !id.isBlank(), "User id can't be empty");
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST.value(), USERNOTFOUND + id));

        if (user.getProfileImage() == null) {
            user.setProfileImage(appbaseUrl + CONTROLLER_ENDPOINT + "/me/profile-image");
        }

        return user.toDTO();
    }

    @Override
    public UserDTO findByUserName(String name) throws AppException {
        Preconditions.checkArgument(name != null && !name.isBlank(), "User name can't be empty");
        AppUser user = userRepository.findByUserName(name);
        if (user == null) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), USERNOTFOUND);
        }
        if (user.getProfileImage() == null) {
            user.setProfileImage(appbaseUrl + CONTROLLER_ENDPOINT + "/me/profile-image");
        }
        return user.toDTO();
    }

    @Override
    public UserDTO findByUserEmail(String email) throws AppException {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST.value(), "Email not found"));

        UserDTO userDetails = null;
        if (user != null) {
            userDetails = user.toDTO();
        }
        if (user.getProfileImage() == null) {
            user.setProfileImage(appbaseUrl + CONTROLLER_ENDPOINT + "/me/profile-image");
        }
        return userDetails;
    }

    private void checkAndUpdateUser(AppUser oldData, AppUser newData) {
        if (newData.getUserName() == null || newData.getUserName().isBlank()) {
            newData.setUserName(oldData.getUserName());
        }
        if (newData.getFirstName() == null || newData.getFirstName().isBlank()) {
            newData.setFirstName(oldData.getFirstName());
        }
        if (newData.getLastName() == null || newData.getLastName().isBlank()) {
            newData.setLastName(oldData.getLastName());
        }
        if (newData.getEmail() == null || newData.getEmail().isBlank()) {
            newData.setEmail(oldData.getEmail());
        }
        if (newData.getProfileImage() == null || newData.getProfileImage().isBlank()) {
            newData.setProfileImage(oldData.getProfileImage());
        }
        if (newData.getDob() == null && oldData.getDob() != null) {
            newData.setDob(oldData.getDob());
        }
        if (newData.getAge() < 0) {
            newData.setAge(oldData.getAge());
        }
        if (newData.getMobile() == null || newData.getMobile().isBlank()) {
            newData.setMobile(oldData.getMobile());
        }
        newData.setProvider(oldData.getProvider());
        newData.setPassword(oldData.getPassword());
    }
}
