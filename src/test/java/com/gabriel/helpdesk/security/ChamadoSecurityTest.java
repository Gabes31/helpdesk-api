package com.gabriel.helpdesk.security;

import com.gabriel.helpdesk.config.SecurityConfig;

import com.gabriel.helpdesk.controller.ChamadoController;

import com.gabriel.helpdesk.service.ChamadoService;
import com.gabriel.helpdesk.service.ComentarioChamadoService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.context.annotation.Import;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.test.context.TestPropertySource;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChamadoController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {"app.jwt.secret=chave-de-teste-helpdesk-123456789012345678901234567890"})
class ChamadoSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChamadoService chamadoService;

    @MockitoBean
    private ComentarioChamadoService comentarioService;

    @Test
    void semJwtDeveRetornar401() throws Exception {
        mockMvc.perform(get("/chamados")).andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNaoPodeListarTodosOsChamados() throws Exception {

        mockMvc.perform(
                get("/chamados")
                        .with(jwt()
                                .jwt(jwt -> jwt.subject("cliente@email.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))).andExpect(status().isForbidden());
    }


    @Test
    void adminPodeListarTodosOsChamados() throws Exception {
        when(chamadoService.listarTodos("admin@helpdesk.com")).thenReturn(List.of());

        mockMvc.perform(
                get("/chamados")
                        .with(jwt()
                                .jwt(jwt -> jwt.subject("admin@helpdesk.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))).andExpect(status().isOk()).andExpect(content().json("[]"));
    }
}