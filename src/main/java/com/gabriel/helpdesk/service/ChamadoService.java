package com.gabriel.helpdesk.service;

import com.gabriel.helpdesk.dto.*;
import com.gabriel.helpdesk.exception.AcessoNegadoException;
import com.gabriel.helpdesk.exception.RecursoNaoEncontradoException;
import com.gabriel.helpdesk.exception.RegraNegocioException;

import com.gabriel.helpdesk.model.*;

import com.gabriel.helpdesk.repository.ChamadoRepository;
import com.gabriel.helpdesk.repository.HistoricoChamadoRepository;
import com.gabriel.helpdesk.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistoricoChamadoRepository historicoRepository;


    public ChamadoService(ChamadoRepository chamadoRepository, UsuarioRepository usuarioRepository, HistoricoChamadoRepository historicoRepository) {

        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.historicoRepository = historicoRepository;
    }


    public List<ChamadoResponseDTO> listarTodos(String email) {

        Usuario usuario = buscarUsuarioAtivoPorEmail(email);

        if (usuario.getPerfil() != PerfilUsuario.ADMIN) {
            throw new AcessoNegadoException("Apenas administradores podem listar todos os chamados");
        }

        return chamadoRepository.findAll().stream().map(this::converterParaResponseDTO).toList();
    }

    public List<ChamadoResponseDTO> listarMeus(String email) {

        Usuario usuario = buscarUsuarioAtivoPorEmail(email);
        List<Chamado> chamados;

        if (usuario.getPerfil() == PerfilUsuario.CLIENTE) {

            chamados = chamadoRepository.findBySolicitanteIdOrderByIdDesc(usuario.getId());

        } else if (usuario.getPerfil() == PerfilUsuario.SUPORTE) {

            chamados = chamadoRepository.findByResponsavelIdOrderByIdDesc(usuario.getId());

        } else {

            chamados = chamadoRepository.findAll();
        }

        return chamados.stream().map(this::converterParaResponseDTO).toList();
    }


    public List<ChamadoResponseDTO> listarDisponiveis(String email) {

        Usuario usuario = buscarUsuarioAtivoPorEmail(email);
        validarSuporte(usuario);

        List<StatusChamado> status = List.of(StatusChamado.ABERTO, StatusChamado.EM_ANALISE);

        return chamadoRepository.findByResponsavelIsNullAndStatusInOrderByIdAsc(status).stream().map(this::converterParaResponseDTO).toList();
    }


    public ChamadoResponseDTO buscarPorIdAutorizado(Long chamadoId, String email) {

        Chamado chamado = buscarChamado(chamadoId);
        Usuario usuario = buscarUsuarioAtivoPorEmail(email);
        validarVisualizacao(chamado, usuario);

        return converterParaResponseDTO(chamado);
    }


    public ChamadoResponseDTO criar(CriarChamadoDTO dto, String email) {

        Usuario solicitante = buscarUsuarioAtivoPorEmail(email);

        if (solicitante.getPerfil() != PerfilUsuario.CLIENTE) {

            throw new AcessoNegadoException("Apenas usuários CLIENTE podem abrir chamados");
        }

        Chamado chamado = new Chamado();
        chamado.setTitulo(dto.getTitulo());
        chamado.setDescricao(dto.getDescricao());
        chamado.setPrioridade(dto.getPrioridade());
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setSolicitante(solicitante);
        Chamado chamadoSalvo = chamadoRepository.save(chamado);
        registrarHistorico(chamadoSalvo, solicitante, TipoEventoChamado.ABERTURA, "Chamado aberto pelo solicitante");

        return converterParaResponseDTO(chamadoSalvo);
    }


    public ChamadoResponseDTO atualizar(Long chamadoId, AtualizarChamadoDTO dto, String email) {

        Chamado chamado = buscarChamado(chamadoId);
        Usuario usuario = buscarUsuarioAtivoPorEmail(email);

        if (chamado.getSolicitante() == null || !chamado.getSolicitante().getId().equals(usuario.getId())) {

            throw new AcessoNegadoException("Apenas o solicitante pode editar este chamado");
        }

        if (chamado.getStatus() != StatusChamado.ABERTO) {

            throw new RegraNegocioException("Apenas chamados ABERTOS podem ser editados");
        }

        chamado.setTitulo(dto.getTitulo());
        chamado.setDescricao(dto.getDescricao());
        chamado.setPrioridade(dto.getPrioridade());
        Chamado chamadoSalvo = chamadoRepository.save(chamado);

        return converterParaResponseDTO(chamadoSalvo);
    }


    public ChamadoResponseDTO iniciarAnalise(Long chamadoId, String email) {

        Chamado chamado = buscarChamado(chamadoId);
        Usuario usuario = buscarUsuarioAtivoPorEmail(email);

        validarSuporte(usuario);

        if (chamado.getStatus() != StatusChamado.ABERTO) {

            throw new RegraNegocioException("Apenas chamados ABERTOS podem entrar em análise");
        }

        chamado.setStatus(StatusChamado.EM_ANALISE);
        Chamado salvo = chamadoRepository.save(chamado);
        registrarHistorico(salvo, usuario, TipoEventoChamado.ANALISE_INICIADA, "Análise do chamado iniciada");

        return converterParaResponseDTO(salvo);
    }


    public ChamadoResponseDTO assumirChamado(Long chamadoId, String email) {

        Chamado chamado = buscarChamado(chamadoId);
        Usuario responsavel = buscarUsuarioAtivoPorEmail(email);

        validarSuporte(responsavel);

        if (chamado.getResponsavel() != null) {

            throw new RegraNegocioException("Este chamado já possui um responsável");
        }

        if (chamado.getStatus() != StatusChamado.ABERTO && chamado.getStatus() != StatusChamado.EM_ANALISE) {

            throw new RegraNegocioException("O chamado não pode ser assumido no status atual");
        }

        chamado.setResponsavel(responsavel);
        chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
        Chamado salvo = chamadoRepository.save(chamado);
        registrarHistorico(salvo, responsavel, TipoEventoChamado.ASSUMIDO, "Chamado assumido por " + responsavel.getNome());

        return converterParaResponseDTO(salvo);
    }


    public ChamadoResponseDTO aguardarCliente(Long chamadoId, String email) {

        Chamado chamado = buscarChamado(chamadoId);
        Usuario usuario = buscarUsuarioAtivoPorEmail(email);

        validarSuporte(usuario);
        validarResponsavel(chamado, usuario);


        if (chamado.getStatus() != StatusChamado.EM_ATENDIMENTO) {

            throw new RegraNegocioException("Apenas chamados EM_ATENDIMENTO podem aguardar o cliente");
        }

        chamado.setStatus(StatusChamado.AGUARDANDO_CLIENTE);
        Chamado salvo = chamadoRepository.save(chamado);
        registrarHistorico(salvo, usuario, TipoEventoChamado.AGUARDANDO_CLIENTE, "Atendimento aguardando retorno do cliente");

        return converterParaResponseDTO(salvo);
    }


    public ChamadoResponseDTO retomarAtendimento(Long chamadoId, String email) {

        Chamado chamado = buscarChamado(chamadoId);
        Usuario usuario = buscarUsuarioAtivoPorEmail(email);

        validarSuporte(usuario);
        validarResponsavel(chamado, usuario);


        if (chamado.getStatus() != StatusChamado.AGUARDANDO_CLIENTE) {

            throw new RegraNegocioException("Apenas chamados AGUARDANDO_CLIENTE podem ser retomados");
        }

        chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
        Chamado salvo = chamadoRepository.save(chamado);
        registrarHistorico(salvo, usuario, TipoEventoChamado.ATENDIMENTO_RETOMADO, "Atendimento retomado");

        return converterParaResponseDTO(salvo);
    }


    public ChamadoResponseDTO resolverChamado(Long chamadoId, String email) {

        Chamado chamado = buscarChamado(chamadoId);
        Usuario usuario = buscarUsuarioAtivoPorEmail(email);

        validarSuporte(usuario);
        validarResponsavel(chamado, usuario);


        if (chamado.getStatus() != StatusChamado.EM_ATENDIMENTO) {

            throw new RegraNegocioException("Apenas chamados EM_ATENDIMENTO podem ser resolvidos");
        }

        chamado.setStatus(StatusChamado.RESOLVIDO);
        Chamado salvo = chamadoRepository.save(chamado);
        registrarHistorico(salvo, usuario, TipoEventoChamado.RESOLVIDO, "Chamado marcado como resolvido");

        return converterParaResponseDTO(salvo);
    }


    public ChamadoResponseDTO fecharChamado(Long chamadoId, String email) {

        Chamado chamado = buscarChamado(chamadoId);
        Usuario usuario = buscarUsuarioAtivoPorEmail(email);


        if (chamado.getSolicitante() == null || !chamado.getSolicitante().getId().equals(usuario.getId())) {

            throw new AcessoNegadoException("Apenas o solicitante pode fechar o chamado");
        }


        if (chamado.getStatus() != StatusChamado.RESOLVIDO) {

            throw new RegraNegocioException("Apenas chamados RESOLVIDOS podem ser fechados");
        }

        chamado.setStatus(StatusChamado.FECHADO);
        Chamado salvo = chamadoRepository.save(chamado);
        registrarHistorico(salvo, usuario, TipoEventoChamado.FECHADO, "Chamado fechado pelo solicitante");

        return converterParaResponseDTO(salvo);
    }


    public List<HistoricoChamadoResponseDTO> listarHistorico(Long chamadoId, String email) {

        Chamado chamado = buscarChamado(chamadoId);
        Usuario usuario = buscarUsuarioAtivoPorEmail(email);
        validarVisualizacao(chamado, usuario);

        return historicoRepository.findByChamadoIdOrderByDataHoraAsc(chamadoId).stream().map(this::converterHistoricoParaDTO).toList();
    }


    private Chamado buscarChamado(Long id) {

        return chamadoRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Chamado não encontrado"));
    }


    private Usuario buscarUsuarioAtivoPorEmail(String email) {

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado"));

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {

            throw new AcessoNegadoException("Usuário está inativo");
        }


        return usuario;
    }


    private void validarSuporte(Usuario usuario) {

        if (usuario.getPerfil() != PerfilUsuario.SUPORTE) {

            throw new AcessoNegadoException("Apenas usuários SUPORTE podem executar esta ação");
        }
    }


    private void validarResponsavel(Chamado chamado, Usuario usuario) {

        if (chamado.getResponsavel() == null) {

            throw new RegraNegocioException("O chamado ainda não possui responsável");
        }


        if (!chamado.getResponsavel().getId().equals(usuario.getId())) {

            throw new AcessoNegadoException("Apenas o responsável pelo chamado pode executar esta ação");
        }
    }


    private void validarVisualizacao(Chamado chamado, Usuario usuario) {

        if (usuario.getPerfil() == PerfilUsuario.ADMIN) {
            return;
        }


        if (usuario.getPerfil() == PerfilUsuario.CLIENTE) {

            boolean ehSolicitante = chamado.getSolicitante() != null && chamado.getSolicitante().getId().equals(usuario.getId());


            if (!ehSolicitante) {

                throw new AcessoNegadoException("Você não possui acesso a este chamado");
            }

            return;
        }


        if (usuario.getPerfil() == PerfilUsuario.SUPORTE) {

            boolean ehResponsavel = chamado.getResponsavel() != null && chamado.getResponsavel().getId().equals(usuario.getId());
            boolean estaDisponivel = chamado.getResponsavel() == null && (chamado.getStatus() == StatusChamado.ABERTO || chamado.getStatus() == StatusChamado.EM_ANALISE);

            if (!ehResponsavel && !estaDisponivel) {

                throw new AcessoNegadoException("Você não possui acesso a este chamado");
            }
        }
    }


    private void registrarHistorico(Chamado chamado, Usuario usuario, TipoEventoChamado tipoEvento, String descricao) {

        HistoricoChamado historico = new HistoricoChamado();

        historico.setChamado(chamado);
        historico.setUsuario(usuario);
        historico.setTipoEvento(tipoEvento);
        historico.setDescricao(descricao);

        historico.setDataHora(LocalDateTime.now());

        historicoRepository.save(historico);
    }


    private ChamadoResponseDTO converterParaResponseDTO(Chamado chamado) {

        return new ChamadoResponseDTO(chamado.getId(), chamado.getTitulo(), chamado.getDescricao(), chamado.getPrioridade(), chamado.getStatus(),
                converterUsuarioParaResumo(chamado.getSolicitante()), converterUsuarioParaResumo(chamado.getResponsavel()));
    }


    private UsuarioResumoDTO converterUsuarioParaResumo(Usuario usuario) {

        if (usuario == null) {
            return null;
        }

        return new UsuarioResumoDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil());
    }


    private HistoricoChamadoResponseDTO converterHistoricoParaDTO(HistoricoChamado historico) {

        return new HistoricoChamadoResponseDTO(historico.getId(), historico.getTipoEvento(), historico.getDescricao(), historico.getDataHora(), converterUsuarioParaResumo(historico.getUsuario()));
    }
}