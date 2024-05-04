package com.vapps.auth.service;

import com.vapps.auth.dto.UserDTO;
import com.vapps.auth.exception.AppException;

public interface AppUserService {
    
    String createUser(UserDTO user) throws AppException;

    void deleteUser(String userId);

    void updateUser(UserDTO user) throws AppException;

    UserDTO findByUserId(String id) throws AppException;

    UserDTO findByUserName(String name) throws AppException;

    UserDTO findByUserEmail(String email) throws AppException;
   
}
