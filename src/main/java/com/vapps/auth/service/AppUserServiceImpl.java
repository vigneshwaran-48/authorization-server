package com.vapps.auth.service;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import com.vapps.auth.dto.UserDTO;
import com.vapps.auth.exception.AppException;
import com.vapps.auth.model.AppUser;
import com.vapps.auth.model.AppUserPrincipal;
import com.vapps.auth.repository.UserRepository;

@Service
public class AppUserServiceImpl implements UserDetailsManager, AppUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = userRepository.findByUserName(username);

        if (appUser == null) {
            appUser = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
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
        AppUser appUser = userRepository.findByUserName(username);
        if (appUser != null && appUser.getUserName().equals(username)) {
            return true;
        }
        return false;
    }

    private AppUser toAppUser(UserDetails user) {

        AppUser appUser = new AppUser();

        appUser.setUserName(user.getUsername());
        appUser.setPassword(user.getPassword());

        return appUser;
    }

    @Override
    public String createUser(UserDTO user) throws AppException {
        AppUser appUser = AppUser.build(user);

        if (userExists(appUser.getUserName())) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "User name exists");
        }
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        AppUser createdUser = userRepository.save(appUser);

        if (createdUser == null) {
            throw new InternalError("Error in creating the user");
        }
        return createdUser.getId();
    }

    @Override
    public void updateUser(UserDTO user) {
        AppUser appUser = AppUser.build(user);

        if (!userExists(appUser.getUserName()) && findByUserId(appUser.getId()) == null) {
            throw new UsernameNotFoundException("User not found");
        }
        userRepository.save(appUser);
    }

    @Override
    public UserDTO findByUserId(String id) {
        AppUser user = userRepository.findById(id).orElse(null);
        return user != null ? user.toDTO() : null;
    }

    @Override
    public UserDTO findByUserName(String name) {
        AppUser user = userRepository.findByUserName(name);
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
        return userDetails;
    }

}
