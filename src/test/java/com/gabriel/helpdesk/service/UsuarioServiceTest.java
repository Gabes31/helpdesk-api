package com.gabriel.helpdesk.service;

import com.gabriel.helpdesk.dto.CriarUsuarioDTO;
import com.gabriel.helpdesk.dto.UsuarioResponseDTO;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;


    @Test
    void adminDeveCriarUsuarioComSenhaCriptografada() {

        Usuario admin = new Usuario();

        admin.setId(1L);
        admin.setNome("Administrador");
        admin.setEmail("admin@helpdesk.com");
        admin.setPerfil(PerfilUsuario.ADMIN);
        admin.setAtivo(true);

        CriarUsuarioDTO dto = new CriarUsuarioDTO();

        dto.setNome("Arthur");
        dto.setEmail("ARTHUR@email.com");
        dto.setSenha("Senha12345");
        dto.setPerfil(PerfilUsuario.SUPORTE);

        when(usuarioRepository.findByEmailIgnoreCase("admin@helpdesk.com")).thenReturn(Optional.of(admin));
        when(usuarioRepository.existsByEmailIgnoreCase("arthur@email.com")).thenReturn(false);
        when(passwordEncoder.encode("Senha12345")).thenReturn("senha-criptografada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(2L);
            return usuario;
        });

        UsuarioResponseDTO resposta = usuarioService.criar(dto, "admin@helpdesk.com");

        assertEquals(2L, resposta.getId());
        assertEquals("arthur@email.com", resposta.getEmail());
        assertEquals(PerfilUsuario.SUPORTE, resposta.getPerfil());
        assertTrue(resposta.getAtivo());

        verify(passwordEncoder, times(1)).encode("Senha12345");
    }


    @Test
    void adminNaoPodeDesativarASiMesmo() {

        Usuario admin = new Usuario();

        admin.setId(1L);
        admin.setNome("Administrador");
        admin.setEmail("admin@helpdesk.com");
        admin.setPerfil(PerfilUsuario.ADMIN);
        admin.setAtivo(true);

        when(usuarioRepository.findByEmailIgnoreCase("admin@helpdesk.com")).thenReturn(Optional.of(admin));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        assertThrows(RegraNegocioException.class, () -> usuarioService.desativar(1L, "admin@helpdesk.com"));

        verify(usuarioRepository, never()).save(any());
    }
}