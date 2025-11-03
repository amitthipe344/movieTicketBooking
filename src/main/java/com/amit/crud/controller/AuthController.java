package com.amit.crud.controller;

import com.amit.crud.dto.AuthRequest;
import com.amit.crud.dto.AuthResponse;
import com.amit.crud.dto.RegisterRequest;
import com.amit.crud.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        authService.register(req, false);
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest req) {
        authService.register(req, true);
        return ResponseEntity.ok("Admin registered");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}
