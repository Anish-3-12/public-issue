package com.publicissuetracker.service.impl;

import com.publicissuetracker.dto.UserLoginRequest;
import com.publicissuetracker.dto.UserResponse;
import com.publicissuetracker.dto.UserSignupRequest;
import com.publicissuetracker.model.User;
import com.publicissuetracker.repository.UserRepository;
import com.publicissuetracker.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Simple UserService implementation.
 * - Uses BCrypt to hash passwords (injected bean).
 * - Uses UserRepository to save / lookup users.
 *
 * We keep things intentionally simple for now; later we'll add token generation (JWT),
 * better error types, and more validation.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse signup(UserSignupRequest req) {
        // 1) check if email already used
        if (userRepository.existsByEmail(req.email)) {
            throw new IllegalStateException("Email already registered");
        }

        // 2) hash the password
        String hashed = passwordEncoder.encode(req.password);

        // 3) create entity and save
        User user = new User(req.name, req.email, hashed, "CITIZEN"); // default role CITIZEN
        User saved = userRepository.save(user);

        // 4) convert to response DTO
        return toResponse(saved);
    }

    @Override
    public Optional<UserResponse> authenticate(UserLoginRequest req) {
        Optional<User> maybe = userRepository.findByEmail(req.email);
        if (maybe.isEmpty()) {
            return Optional.empty();
        }
        User user = maybe.get();
        // check password
        if (!passwordEncoder.matches(req.password, user.getPasswordHash())) {
            return Optional.empty();
        }
        return Optional.of(toResponse(user));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> findById(String userId) {
        return userRepository.findById(userId).map(this::toResponse);
    }

    // helper to convert User entity -> UserResponse DTO
    private UserResponse toResponse(User u) {
        UserResponse r = new UserResponse();
        r.id = u.getId();
        r.name = u.getName();
        r.email = u.getEmail();
        r.role = u.getRole();
        r.createdAt = u.getCreatedAt();
        r.updatedAt = u.getUpdatedAt();
        return r;
    }
}
