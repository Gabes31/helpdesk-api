package com.gabriel.helpdesk.dto;

import com.gabriel.helpdesk.model.PerfilUsuario;

public class UsuarioResponseDTO {

    private Long id;
    private String nome;
    private String email;
    private PerfilUsuario perfil;
    private Boolean ativo;

    public UsuarioResponseDTO(Long id, String nome, String email, PerfilUsuario perfil, Boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
        this.ativo = ativo;

    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public Boolean getAtivo() {
        return ativo;
    }
}
