package com.gabriel.helpdesk.controller;

import com.gabriel.helpdesk.dto.LoginDTO;
import com.gabriel.helpdesk.dto.LoginResponseDTO;

import com.gabriel.helpdesk.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO dto) {

        return ResponseEntity.ok(authService.login(dto));
    }
}