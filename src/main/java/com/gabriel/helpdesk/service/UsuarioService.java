package com.gabriel.helpdesk.service;

import com.gabriel.helpdesk.dto.CriarUsuarioDTO;
import com.gabriel.helpdesk.dto.UsuarioResponseDTO;

import com.gabriel.helpdesk.exception.RecursoNaoEncontradoException;
import com.gabriel.helpdesk.exception.RegraNegocioException;

import com.gabriel.helpdesk.model.PerfilUsuario;
import com.gabriel.helpdesk.model.Usuario;

import com.gabriel.helpdesk.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // =====================================================
    // LISTAR TODOS
    // =====================================================

    public List<UsuarioResponseDTO> listarTodos(String emailAdmin) {

        Usuario admin = buscarUsuarioAtivoPorEmail(emailAdmin);
        validarAdmin(admin);

        return usuarioRepository.findAllByOrderByNomeAsc().stream().map(this::converterParaResponseDTO).toList();
    }


    // =====================================================
    // BUSCAR POR ID
    // =====================================================

    public UsuarioResponseDTO buscarPorId(Long id, String emailAdmin) {

        Usuario admin = buscarUsuarioAtivoPorEmail(emailAdmin);
        validarAdmin(admin);

        Usuario usuario = buscarUsuario(id);

        return converterParaResponseDTO(usuario);
    }


    // =====================================================
    // CRIAR
    // =====================================================

    public UsuarioResponseDTO criar(CriarUsuarioDTO dto, String emailAdmin) {

        Usuario admin = buscarUsuarioAtivoPorEmail(emailAdmin);
        validarAdmin(admin);
        String email = dto.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {

            throw new RegraNegocioException("Já existe um usuário cadastrado com este email");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setPerfil(dto.getPerfil());
        usuario.setAtivo(true);
        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return converterParaResponseDTO(usuarioSalvo);
    }


    // =====================================================
    // DESATIVAR
    // =====================================================

    public UsuarioResponseDTO desativar(Long usuarioId, String emailAdmin) {

        Usuario admin = buscarUsuarioAtivoPorEmail(emailAdmin);
        validarAdmin(admin);

        Usuario usuario = buscarUsuario(usuarioId);

        if (admin.getId().equals(usuario.getId())) {

            throw new RegraNegocioException("O administrador não pode desativar o próprio usuário");
        }

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {

            throw new RegraNegocioException("O usuário já está inativo");
        }

        usuario.setAtivo(false);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return converterParaResponseDTO(usuarioSalvo);
    }


    // =====================================================
    // REATIVAR
    // =====================================================

    public UsuarioResponseDTO reativar(Long usuarioId, String emailAdmin) {

        Usuario admin = buscarUsuarioAtivoPorEmail(emailAdmin);
        validarAdmin(admin);

        Usuario usuario = buscarUsuario(usuarioId);

        if (Boolean.TRUE.equals(usuario.getAtivo())) {

            throw new RegraNegocioException("O usuário já está ativo");
        }

        usuario.setAtivo(true);
        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return converterParaResponseDTO(usuarioSalvo);
    }


    // =====================================================
    // BUSCAR USUÁRIO
    // =====================================================

    private Usuario buscarUsuario(Long id) {

        return usuarioRepository.findById(id).orElseThrow(() ->
                new RecursoNaoEncontradoException("Usuário não encontrado"));
    }


    // =====================================================
    // BUSCAR USUÁRIO AUTENTICADO
    // =====================================================

    private Usuario buscarUsuarioAtivoPorEmail(String email) {

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow(() ->
                new RecursoNaoEncontradoException("Usuário autenticado não encontrado"));

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {

            throw new RegraNegocioException("Usuário autenticado está inativo");
        }

        return usuario;
    }


    // =====================================================
    // VALIDAR ADMIN
    // =====================================================

    private void validarAdmin(Usuario usuario) {

        if (usuario.getPerfil() != PerfilUsuario.ADMIN) {

            throw new RegraNegocioException("Apenas administradores podem executar esta ação");
        }
    }


    // =====================================================
    // CONVERTER
    // =====================================================

    private UsuarioResponseDTO converterParaResponseDTO(Usuario usuario) {

        return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil(), usuario.getAtivo());
    }
}