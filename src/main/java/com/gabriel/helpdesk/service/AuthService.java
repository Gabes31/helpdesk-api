package com.gabriel.helpdesk.service;

import com.gabriel.helpdesk.dto.LoginDTO;
import com.gabriel.helpdesk.dto.LoginResponseDTO;
import com.gabriel.helpdesk.dto.UsuarioResumoDTO;

import com.gabriel.helpdesk.exception.CredenciaisInvalidasException;
import com.gabriel.helpdesk.exception.RegraNegocioException;

import com.gabriel.helpdesk.model.Usuario;

import com.gabriel.helpdesk.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;

        this.passwordEncoder = passwordEncoder;

        this.tokenService = tokenService;
    }

    public LoginResponseDTO login(LoginDTO dto) {

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(dto.getEmail()).orElseThrow(() -> new CredenciaisInvalidasException("Email ou senha inválidos"));

        if (usuario.getSenha() == null || !passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new CredenciaisInvalidasException("Email ou senha inválidos");
        }

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new RegraNegocioException("Usuário está inativo");
        }

        String token = tokenService.gerarToken(usuario);

        UsuarioResumoDTO usuarioDTO = new UsuarioResumoDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil());

        return new LoginResponseDTO(token, usuarioDTO);
    }
}