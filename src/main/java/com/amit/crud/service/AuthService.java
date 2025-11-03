package com.amit.crud.service;

import com.amit.crud.dto.AuthRequest;
import com.amit.crud.dto.AuthResponse;
import com.amit.crud.dto.RegisterRequest;
import com.amit.crud.entity.Role;
import com.amit.crud.entity.User;
import com.amit.crud.exception.BadRequestException;
import com.amit.crud.exception.NotFoundException;
import com.amit.crud.repository.UserRepository;
import com.amit.crud.config.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public void register(RegisterRequest req, boolean admin) {
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            throw new BadRequestException("Username cannot be empty");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new BadRequestException("Password cannot be empty");
        }
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new BadRequestException("Username already taken");
        }
        User u = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(admin ? Set.of(Role.ADMIN) : Set.of(Role.CUSTOMER))
                .build();
        userRepository.save(u);
    }

    public AuthResponse login(AuthRequest req) {
        if (req.getUsername() == null || req.getUsername().isBlank() ||
                req.getPassword() == null || req.getPassword().isBlank()) {
            throw new BadRequestException("Username and password must be provided");
        }

        var user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found with username: " + req.getUsername()));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid username/password");
        }
        String token = jwtUtils.generateToken(user.getUsername());
        return new AuthResponse(token);
    }
}
