package com.gabriel.helpdesk.dto;

import com.gabriel.helpdesk.model.TipoEventoChamado;

import java.time.LocalDateTime;

public class HistoricoChamadoResponseDTO {

    private Long id;
    private TipoEventoChamado tipoEvento;
    private String descricao;
    private LocalDateTime dataHora;
    private UsuarioResumoDTO usuario;

    public HistoricoChamadoResponseDTO(Long id, TipoEventoChamado tipoEvento, String descricao, LocalDateTime dataHora, UsuarioResumoDTO usuario) {
        this.id = id;
        this.tipoEvento = tipoEvento;
        this.descricao = descricao;
        this.dataHora = dataHora;
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public TipoEventoChamado getTipoEvento() {
        return tipoEvento;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public UsuarioResumoDTO getUsuario() {
        return usuario;
    }
}