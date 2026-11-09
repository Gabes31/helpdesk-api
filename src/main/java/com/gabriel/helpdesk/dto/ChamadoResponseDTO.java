package com.gabriel.helpdesk.dto;

import com.gabriel.helpdesk.model.Prioridade;
import com.gabriel.helpdesk.model.StatusChamado;

public class ChamadoResponseDTO {

    private Long id;
    private String titulo;
    private String descricao;
    private Prioridade prioridade;
    private StatusChamado status;

    private UsuarioResumoDTO solicitante;
    private UsuarioResumoDTO responsavel;

    public ChamadoResponseDTO(Long id, String titulo, String descricao, Prioridade prioridade, StatusChamado status,  UsuarioResumoDTO solicitante, UsuarioResumoDTO responsavel) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.status = status;
        this.solicitante = solicitante;
        this.responsavel = responsavel;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public StatusChamado getStatus() {
        return status;
    }

    public UsuarioResumoDTO getSolicitante() {
        return solicitante;
    }

    public UsuarioResumoDTO getResponsavel() {
        return responsavel;
    }
}
