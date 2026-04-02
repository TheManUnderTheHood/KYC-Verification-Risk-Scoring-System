package com.example.kycverificationandscoringsystem.service;

import com.example.kycverificationandscoringsystem.dto.AuthResponse;
import com.example.kycverificationandscoringsystem.dto.LoginRequest;
import com.example.kycverificationandscoringsystem.dto.RegisterRequest;
import com.example.kycverificationandscoringsystem.entity.User;
import com.example.kycverificationandscoringsystem.enums.Role;
import com.example.kycverificationandscoringsystem.repository.UserRepository;
import com.example.kycverificationandscoringsystem.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered!"); // Changed to standard exception
        }

        Role assignedRole = request.getRole() != null ? request.getRole() : Role.USER;

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(assignedRole)
                .build();

        userRepository.save(user);

        var userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()))
        );
        String jwtToken = jwtUtils.generateToken(userDetails);

        return new AuthResponse(jwtToken, "User registered successfully!", user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found")); // Changed to standard exception

        var userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()))
        );

        String jwtToken = jwtUtils.generateToken(userDetails);
        return new AuthResponse(jwtToken, "Login successful!", user.getRole().name());
    }
}
