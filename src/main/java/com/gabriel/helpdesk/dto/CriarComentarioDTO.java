package com.gabriel.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CriarComentarioDTO {

    @NotBlank(message = "A mensagem é obrigatória")
    @Size(min = 2, max = 2000, message = "A mensagem deve ter entre 2 e 2000 caracteres")
    private String mensagem;


    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}