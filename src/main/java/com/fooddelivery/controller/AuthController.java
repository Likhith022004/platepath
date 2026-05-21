package com.fooddelivery.controller;

import com.fooddelivery.dto.request.LoginRequest;
import com.fooddelivery.dto.request.RegisterRequest;
import com.fooddelivery.dto.response.JwtResponse;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.UserRole;
import com.fooddelivery.repository.UserRepository;
import com.fooddelivery.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // ===== POST /api/auth/register =====
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(request.getRole() != null ? request.getRole() : UserRole.CUSTOMER)
                .build();

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);

        log.info("User registered successfully: id={}, role={}", saved.getId(), saved.getRole());

        return ResponseEntity.status(HttpStatus.CREATED).body(JwtResponse.builder()
                .token(token)
                .expiresIn(jwtService.getExpirationMs())
                .userId(saved.getId())
                .userName(saved.getName())
                .userEmail(saved.getEmail())
                .userRole(saved.getRole())
                .build());
    }

    // ===== POST /api/auth/login =====
    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()));

            User user = java.util.Objects.requireNonNull(
                    (User) authentication.getPrincipal(), "Authentication principal must not be null");
            String token = jwtService.generateToken(user);

            log.info("Login successful for user id={}", user.getId());

            return ResponseEntity.ok(JwtResponse.builder()
                    .token(token)
                    .expiresIn(jwtService.getExpirationMs())
                    .userId(user.getId())
                    .userName(user.getName())
                    .userEmail(user.getEmail())
                    .userRole(user.getRole())
                    .build());

        } catch (BadCredentialsException ex) {
            log.warn("Login failed for email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of(
                            "status", 401,
                            "message", "Invalid email or password",
                            "timestamp", java.time.LocalDateTime.now().toString()));
        }
    }
}
