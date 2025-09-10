package com.publicissuetracker.api;

import com.publicissuetracker.dto.UserLoginRequest;
import com.publicissuetracker.dto.UserSignupRequest;
import com.publicissuetracker.dto.UserResponse;
import com.publicissuetracker.model.RefreshToken;
import com.publicissuetracker.model.User;
import com.publicissuetracker.security.JwtUtil;
import com.publicissuetracker.service.RefreshTokenService;
import com.publicissuetracker.service.UserService;
import com.publicissuetracker.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public AuthController(UserService userService,
                          JwtUtil jwtUtil,
                          RefreshTokenService refreshTokenService,
                          UserRepository userRepository) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody UserSignupRequest req) {
        UserResponse created = userService.signup(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequest req) {
        return userService.authenticate(req)
                .map(userResp -> {
                    // create access token
                    String accessToken = jwtUtil.generateToken(userResp.id, userResp.email, userResp.role);
                    // create refresh token entity
                    Optional<User> maybeUser = userRepository.findById(userResp.id);
                    if (maybeUser.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "user_not_found"));
                    }
                    User user = maybeUser.get();
                    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
                    return ResponseEntity.ok(Map.of(
                            "accessToken", accessToken,
                            "refreshToken", refreshToken.getToken(),
                            "user", userResp
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "invalid_credentials")));
    }

    /**
     * Exchange a refresh token for a new access token.
     * Body: { "refreshToken": "..." }
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String token = body.get("refreshToken");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "refresh_token_required"));
        }

        Optional<RefreshToken> maybe = refreshTokenService.findValidByToken(token);
        if (maybe.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid_or_expired_refresh_token"));
        }

        RefreshToken rt = maybe.get();
        User user = rt.getUser();
        String newAccess = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

        // Optionally: rotate refresh token (issue a new refresh token and revoke old one)
        // For simplicity we keep existing refresh token valid until expiry OR we can revoke and return new token.
        return ResponseEntity.ok(Map.of(
                "accessToken", newAccess,
                "refreshToken", rt.getToken(),
                "user", Map.of("id", user.getId(), "name", user.getName(), "email", user.getEmail(), "role", user.getRole())
        ));
    }

    /**
     * Revoke a refresh token (logout).
     * Body: { "refreshToken": "..." }
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> body) {
        String token = body.get("refreshToken");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "refresh_token_required"));
        }
        Optional<RefreshToken> maybe = refreshTokenService.findValidByToken(token);
        maybe.ifPresent(refreshTokenService::revoke);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}

