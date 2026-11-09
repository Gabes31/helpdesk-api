package com.gabriel.helpdesk.dto;

import com.gabriel.helpdesk.model.PerfilUsuario;
import com.gabriel.helpdesk.model.Usuario;

public class UsuarioResumoDTO {

    private Long id;
    private String nome;
    private String email;
    private PerfilUsuario perfil;

    public UsuarioResumoDTO(Long id, String nome, String email, PerfilUsuario perfil) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
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
}
