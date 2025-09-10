package com.publicissuetracker.service;

import com.publicissuetracker.dto.UserSignupRequest;
import com.publicissuetracker.dto.UserLoginRequest;
import com.publicissuetracker.dto.UserResponse;
import java.util.Optional;

public interface UserService {

    /**
     * Register a new user (signup).
     * Returns the created user (without password).
     * Throws a runtime exception if email already exists (we'll make a custom one later).
     */
    UserResponse signup(UserSignupRequest req);

    /**
     * Authenticate a user. If credentials are valid, return the UserResponse.
     * In a later step this will return tokens (access/refresh). For now it returns the user.
     */
    Optional<UserResponse> authenticate(UserLoginRequest req);

    /**
     * Find user by id (returns empty Optional if not found).
     */
    Optional<UserResponse> findById(String userId);
}
