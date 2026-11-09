package com.gabriel.helpdesk.exception;

import jakarta.validation.constraints.NotBlank;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
