package com.analytics.platform.controller;

import com.analytics.platform.dto.JwtResponse;
import com.analytics.platform.dto.LoginRequest;
import com.analytics.platform.dto.UserRegistrationDTO;
import com.analytics.platform.entity.UserEntity;
import com.analytics.platform.security.JwtTokenProvider;
import com.analytics.platform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        try {
            UserEntity user = userService.registerUser(registrationDTO);
            return ResponseEntity.ok()
                .body("User registered successfully: " + user.getUsername());
        } catch (Exception e) {
            log.error("Registration error", e);
            return ResponseEntity.badRequest()
                .body("Registration failed: " + e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);
            
            Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
            
            userService.updateLastLogin(loginRequest.getUsername());
            
            return ResponseEntity.ok(new JwtResponse(
                jwt,
                "Bearer",
                authentication.getName(),
                userService.findByUsername(authentication.getName()).getEmail(),
                roles
            ));
        } catch (Exception e) {
            log.error("Login error", e);
            return ResponseEntity.badRequest()
                .body("Login failed: " + e.getMessage());
        }
    }
}
