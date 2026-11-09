package com.gabriel.helpdesk.service;

import com.gabriel.helpdesk.dto.LoginDTO;
import com.gabriel.helpdesk.dto.LoginResponseDTO;

import com.gabriel.helpdesk.exception.CredenciaisInvalidasException;
import com.gabriel.helpdesk.exception.RegraNegocioException;

import com.gabriel.helpdesk.model.PerfilUsuario;
import com.gabriel.helpdesk.model.Usuario;

import com.gabriel.helpdesk.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;


    @Test
    void deveRealizarLoginComSucesso() {

        Usuario usuario = new Usuario();

        usuario.setId(1L);
        usuario.setNome("Gabriel");
        usuario.setEmail("gabriel@email.com");
        usuario.setSenha("senha-criptografada");
        usuario.setPerfil(PerfilUsuario.CLIENTE);
        usuario.setAtivo(true);

        LoginDTO dto = new LoginDTO();

        dto.setEmail("gabriel@email.com");
        dto.setSenha("Senha12345");

        when(usuarioRepository.findByEmailIgnoreCase("gabriel@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Senha12345", "senha-criptografada")).thenReturn(true);
        when(tokenService.gerarToken(usuario)).thenReturn("jwt-de-teste");

        LoginResponseDTO resposta = authService.login(dto);

        assertNotNull(resposta);
        assertEquals("jwt-de-teste", resposta.getToken());
        assertEquals("Bearer", resposta.getTipo());
        assertEquals("Gabriel", resposta.getUsuario().getNome());

        verify(tokenService, times(1)).gerarToken(usuario);
    }


    @Test
    void deveRecusarSenhaIncorreta() {

        Usuario usuario = new Usuario();

        usuario.setEmail("gabriel@email.com");
        usuario.setSenha("senha-criptografada");
        usuario.setAtivo(true);

        LoginDTO dto = new LoginDTO();

        dto.setEmail("gabriel@email.com");
        dto.setSenha("SenhaErrada");

        when(usuarioRepository.findByEmailIgnoreCase("gabriel@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("SenhaErrada", "senha-criptografada")).thenReturn(false);
        assertThrows(CredenciaisInvalidasException.class, () -> authService.login(dto));
        verify(tokenService, never()).gerarToken(any());
    }


    @Test
    void deveRecusarUsuarioInativo() {

        Usuario usuario = new Usuario();

        usuario.setEmail("gabriel@email.com");
        usuario.setSenha("senha-criptografada");
        usuario.setAtivo(false);

        LoginDTO dto = new LoginDTO();

        dto.setEmail("gabriel@email.com");
        dto.setSenha("Senha12345");

        when(usuarioRepository.findByEmailIgnoreCase("gabriel@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Senha12345", "senha-criptografada")).thenReturn(true);
        assertThrows(RegraNegocioException.class, () -> authService.login(dto));
        verify(tokenService, never()).gerarToken(any());
    }
}