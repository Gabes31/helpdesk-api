package com.gabriel.helpdesk.controller;

import com.gabriel.helpdesk.dto.CriarUsuarioDTO;
import com.gabriel.helpdesk.dto.UsuarioResponseDTO;

import com.gabriel.helpdesk.service.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {

        this.usuarioService = usuarioService;
    }


    // =====================================================
    // LISTAR
    // =====================================================

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios(@AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(usuarioService.listarTodos(jwt.getSubject()));
    }


    // =====================================================
    // BUSCAR POR ID
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(usuarioService.buscarPorId(id, jwt.getSubject()));
    }

    // =====================================================
    // CRIAR
    // =====================================================

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CriarUsuarioDTO dto) {
        UsuarioResponseDTO usuario = usuarioService.criar(dto, jwt.getSubject());

        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }


    // =====================================================
    // DESATIVAR
    // =====================================================

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<UsuarioResponseDTO> desativarUsuario(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(usuarioService.desativar(id, jwt.getSubject()));
    }


    // =====================================================
    // REATIVAR
    // =====================================================

    @PatchMapping("/{id}/reativar")
    public ResponseEntity<UsuarioResponseDTO> reativarUsuario(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(usuarioService.reativar(id, jwt.getSubject()));
    }
}