package com.gabriel.helpdesk.service;

import com.gabriel.helpdesk.dto.ComentarioResponseDTO;
import com.gabriel.helpdesk.dto.CriarComentarioDTO;
import com.gabriel.helpdesk.dto.UsuarioResumoDTO;

import com.gabriel.helpdesk.exception.AcessoNegadoException;
import com.gabriel.helpdesk.exception.RecursoNaoEncontradoException;
import com.gabriel.helpdesk.exception.RegraNegocioException;

import com.gabriel.helpdesk.model.*;

import com.gabriel.helpdesk.repository.ChamadoRepository;
import com.gabriel.helpdesk.repository.ComentarioChamadoRepository;
import com.gabriel.helpdesk.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ComentarioChamadoService {

    private final ComentarioChamadoRepository comentarioRepository;
    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;


    public ComentarioChamadoService(ComentarioChamadoRepository comentarioRepository, ChamadoRepository chamadoRepository, UsuarioRepository usuarioRepository) {

        this.comentarioRepository = comentarioRepository;
        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
    }


    public ComentarioResponseDTO comentar(Long chamadoId, CriarComentarioDTO dto, String email) {

        Chamado chamado = buscarChamado(chamadoId);

        Usuario usuario = buscarUsuarioAtivo(email);


        if (chamado.getStatus() == StatusChamado.FECHADO) {

            throw new RegraNegocioException("Não é possível comentar em um chamado fechado");
        }


        validarParticipante(chamado, usuario);


        ComentarioChamado comentario = new ComentarioChamado();

        comentario.setChamado(chamado);
        comentario.setUsuario(usuario);

        comentario.setMensagem(dto.getMensagem());

        comentario.setDataHora(LocalDateTime.now());


        ComentarioChamado salvo = comentarioRepository.save(comentario);


        return converterParaDTO(salvo);
    }


    public List<ComentarioResponseDTO> listarPorChamado(Long chamadoId, String email) {

        Chamado chamado = buscarChamado(chamadoId);

        Usuario usuario = buscarUsuarioAtivo(email);


        validarVisualizacao(chamado, usuario);


        return comentarioRepository.findByChamadoIdOrderByDataHoraAsc(chamadoId).stream().map(this::converterParaDTO).toList();
    }


    private Chamado buscarChamado(Long id) {

        return chamadoRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Chamado não encontrado"));
    }


    private Usuario buscarUsuarioAtivo(String email) {

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));


        if (!Boolean.TRUE.equals(usuario.getAtivo())) {

            throw new AcessoNegadoException("Usuário está inativo");
        }


        return usuario;
    }


    private void validarParticipante(Chamado chamado, Usuario usuario) {

        boolean ehSolicitante = chamado.getSolicitante() != null && chamado.getSolicitante().getId().equals(usuario.getId());


        boolean ehResponsavel = chamado.getResponsavel() != null && chamado.getResponsavel().getId().equals(usuario.getId());


        if (!ehSolicitante && !ehResponsavel) {

            throw new AcessoNegadoException("Apenas o solicitante ou o responsável podem comentar neste chamado");
        }
    }


    private void validarVisualizacao(Chamado chamado, Usuario usuario) {

        if (usuario.getPerfil() == PerfilUsuario.ADMIN) {
            return;
        }


        boolean ehSolicitante = chamado.getSolicitante() != null && chamado.getSolicitante().getId().equals(usuario.getId());


        boolean ehResponsavel = chamado.getResponsavel() != null && chamado.getResponsavel().getId().equals(usuario.getId());


        boolean suportePodeVisualizarDisponivel = usuario.getPerfil() == PerfilUsuario.SUPORTE && chamado.getResponsavel() == null && (chamado.getStatus() == StatusChamado.ABERTO || chamado.getStatus() == StatusChamado.EM_ANALISE);


        if (!ehSolicitante && !ehResponsavel && !suportePodeVisualizarDisponivel) {

            throw new AcessoNegadoException("Você não possui acesso aos comentários deste chamado");
        }
    }


    private ComentarioResponseDTO converterParaDTO(ComentarioChamado comentario) {

        Usuario usuario = comentario.getUsuario();


        UsuarioResumoDTO usuarioDTO = new UsuarioResumoDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil());


        return new ComentarioResponseDTO(comentario.getId(), comentario.getMensagem(), comentario.getDataHora(), usuarioDTO);
    }
}