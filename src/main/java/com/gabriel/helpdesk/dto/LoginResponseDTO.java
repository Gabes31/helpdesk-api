package com.gabriel.helpdesk.dto;

public class LoginResponseDTO {

    private String token;
    private String tipo;
    private UsuarioResumoDTO usuario;

    public LoginResponseDTO(String token, UsuarioResumoDTO usuario) {
        this.token = token;
        this.tipo = "Bearer";
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public String getTipo() {
        return tipo;
    }

    public UsuarioResumoDTO getUsuario() {
        return usuario;
    }
}