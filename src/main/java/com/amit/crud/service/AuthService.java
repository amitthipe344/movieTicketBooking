package com.amit.crud.service;

import com.amit.crud.dto.AuthRequest;
import com.amit.crud.dto.AuthResponse;
import com.amit.crud.dto.RegisterRequest;
import com.amit.crud.entity.Role;
import com.amit.crud.entity.User;
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
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new RuntimeException("Username already taken");
        }
        User u = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(admin ? Set.of(Role.ADMIN) : Set.of(Role.CUSTOMER))
                .build();
        userRepository.save(u);
    }

    public AuthResponse login(AuthRequest req) {
        var user = userRepository.findByUsername(req.getUsername()).orElseThrow(() -> new RuntimeException("Invalid username/password"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) throw new RuntimeException("Invalid username/password");
        String token = jwtUtils.generateToken(user.getUsername());
        return new AuthResponse(token);
    }
}
