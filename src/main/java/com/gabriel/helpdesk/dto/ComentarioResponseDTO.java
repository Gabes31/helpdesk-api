package com.gabriel.helpdesk.dto;

import java.time.LocalDateTime;

public class ComentarioResponseDTO {

    private Long id;
    private String mensagem;
    private LocalDateTime dataHora;
    private UsuarioResumoDTO usuario;

    public ComentarioResponseDTO(
            Long id,
            String mensagem,
            LocalDateTime dataHora,
            UsuarioResumoDTO usuario
    ) {
        this.id = id;
        this.mensagem = mensagem;
        this.dataHora = dataHora;
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public String getMensagem() {
        return mensagem;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public UsuarioResumoDTO getUsuario() {
        return usuario;
    }
}