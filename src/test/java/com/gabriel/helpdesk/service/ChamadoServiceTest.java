package com.gabriel.helpdesk.service;

import com.gabriel.helpdesk.dto.ChamadoResponseDTO;
import com.gabriel.helpdesk.dto.CriarChamadoDTO;

import com.gabriel.helpdesk.exception.AcessoNegadoException;

import com.gabriel.helpdesk.model.*;

import com.gabriel.helpdesk.repository.ChamadoRepository;
import com.gabriel.helpdesk.repository.HistoricoChamadoRepository;
import com.gabriel.helpdesk.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChamadoServiceTest {

    @Mock
    private ChamadoRepository chamadoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private HistoricoChamadoRepository historicoRepository;

    @InjectMocks
    private ChamadoService chamadoService;


    @Test
    void clienteDeveCriarChamadoComoSolicitante() {

        Usuario cliente = criarUsuario(1L, "Gabriel", "gabriel@email.com", PerfilUsuario.CLIENTE);

        CriarChamadoDTO dto = new CriarChamadoDTO();
        dto.setTitulo("Computador não liga");
        dto.setDescricao("O computador não apresenta nenhum sinal ao ligar");
        dto.setPrioridade(Prioridade.ALTA);

        when(usuarioRepository.findByEmailIgnoreCase("gabriel@email.com")).thenReturn(Optional.of(cliente));

        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(invocation -> {
            Chamado chamado = invocation.getArgument(0);
            chamado.setId(10L);
            return chamado;
        });

        when(historicoRepository.save(any(HistoricoChamado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChamadoResponseDTO resposta = chamadoService.criar(dto, "gabriel@email.com");

        assertEquals(10L, resposta.getId());
        assertEquals(StatusChamado.ABERTO, resposta.getStatus());
        assertEquals(1L, resposta.getSolicitante().getId());
        assertNull(resposta.getResponsavel());

        verify(historicoRepository, times(1)).save(any(HistoricoChamado.class));
    }


    @Test
    void suporteDeveAssumirChamadoDisponivel() {

        Usuario cliente = criarUsuario(1L, "Gabriel", "gabriel@email.com", PerfilUsuario.CLIENTE);

        Usuario suporte = criarUsuario(2L, "Arthur", "arthur@email.com", PerfilUsuario.SUPORTE);

        Chamado chamado = new Chamado();
        chamado.setId(10L);
        chamado.setTitulo("Erro no sistema");
        chamado.setDescricao("Sistema não está abrindo corretamente");
        chamado.setPrioridade(Prioridade.ALTA);
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setSolicitante(cliente);


        when(chamadoRepository.findById(10L)).thenReturn(Optional.of(chamado));
        when(usuarioRepository.findByEmailIgnoreCase("arthur@email.com")).thenReturn(Optional.of(suporte));
        when(chamadoRepository.save(chamado)).thenReturn(chamado);
        when(historicoRepository.save(any(HistoricoChamado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChamadoResponseDTO resposta = chamadoService.assumirChamado(10L, "arthur@email.com");

        assertEquals(StatusChamado.EM_ATENDIMENTO, resposta.getStatus());
        assertNotNull(resposta.getResponsavel());
        assertEquals(2L, resposta.getResponsavel().getId());

        verify(chamadoRepository, times(1)).save(chamado);
    }


    @Test
    void clienteNaoPodeVisualizarChamadoDeOutroCliente() {

        Usuario gabriel = criarUsuario(1L, "Gabriel", "gabriel@email.com", PerfilUsuario.CLIENTE);

        Usuario outroCliente = criarUsuario(2L, "João", "joao@email.com", PerfilUsuario.CLIENTE);

        Chamado chamado = new Chamado();

        chamado.setId(20L);
        chamado.setTitulo("Chamado do João");
        chamado.setDescricao("Este chamado pertence a outro usuário");
        chamado.setPrioridade(Prioridade.MEDIA);
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setSolicitante(outroCliente);

        when(chamadoRepository.findById(20L)).thenReturn(Optional.of(chamado));
        when(usuarioRepository.findByEmailIgnoreCase("gabriel@email.com")).thenReturn(Optional.of(gabriel));
        assertThrows(AcessoNegadoException.class, () -> chamadoService.buscarPorIdAutorizado(20L, "gabriel@email.com"));
    }


    private Usuario criarUsuario(Long id, String nome, String email, PerfilUsuario perfil) {

        Usuario usuario = new Usuario();

        usuario.setId(id);
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);

        return usuario;
    }
}