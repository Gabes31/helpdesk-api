package com.gabriel.helpdesk.controller;

import com.gabriel.helpdesk.dto.*;
import com.gabriel.helpdesk.service.ChamadoService;
import com.gabriel.helpdesk.service.ComentarioChamadoService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    private final ChamadoService chamadoService;
    private final ComentarioChamadoService comentarioService;


    public ChamadoController(ChamadoService chamadoService, ComentarioChamadoService comentarioService) {

        this.chamadoService = chamadoService;
        this.comentarioService = comentarioService;
    }


    // ADMIN
    @GetMapping
    public ResponseEntity<List<ChamadoResponseDTO>> listarTodos(@AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(chamadoService.listarTodos(jwt.getSubject()));
    }


    // CLIENTE: chamados abertos por ele
    // SUPORTE: chamados atribuídos a ele
    // ADMIN: todos
    @GetMapping("/meus")
    public ResponseEntity<List<ChamadoResponseDTO>> listarMeus(@AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(chamadoService.listarMeus(jwt.getSubject()));
    }


    // chamados que ainda podem ser assumidos
    @GetMapping("/disponiveis")
    public ResponseEntity<List<ChamadoResponseDTO>> listarDisponiveis(@AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(chamadoService.listarDisponiveis(jwt.getSubject()));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ChamadoResponseDTO> buscarPorId(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(chamadoService.buscarPorIdAutorizado(id, jwt.getSubject()));
    }


    @PostMapping
    public ResponseEntity<ChamadoResponseDTO> criarChamado(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CriarChamadoDTO dto) {

        ChamadoResponseDTO chamado = chamadoService.criar(dto, jwt.getSubject());

        return ResponseEntity.status(HttpStatus.CREATED).body(chamado);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ChamadoResponseDTO> atualizarChamado(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AtualizarChamadoDTO dto) {

        return ResponseEntity.ok(chamadoService.atualizar(id, dto, jwt.getSubject()));
    }


    @PatchMapping("/{id}/analisar")
    public ResponseEntity<ChamadoResponseDTO> iniciarAnalise(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(chamadoService.iniciarAnalise(id, jwt.getSubject()));
    }


    @PatchMapping("/{id}/assumir")
    public ResponseEntity<ChamadoResponseDTO> assumirChamado(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(chamadoService.assumirChamado(id, jwt.getSubject()));
    }


    @PatchMapping("/{id}/aguardar-cliente")
    public ResponseEntity<ChamadoResponseDTO> aguardarCliente(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(chamadoService.aguardarCliente(id, jwt.getSubject()));
    }


    @PatchMapping("/{id}/retomar")
    public ResponseEntity<ChamadoResponseDTO> retomarAtendimento(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(chamadoService.retomarAtendimento(id, jwt.getSubject()));
    }


    @PatchMapping("/{id}/resolver")
    public ResponseEntity<ChamadoResponseDTO> resolverChamado(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(chamadoService.resolverChamado(id, jwt.getSubject()));
    }


    @PatchMapping("/{id}/fechar")
    public ResponseEntity<ChamadoResponseDTO> fecharChamado(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(chamadoService.fecharChamado(id, jwt.getSubject()));
    }


    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoChamadoResponseDTO>> listarHistorico(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(chamadoService.listarHistorico(id, jwt.getSubject()));
    }


    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ComentarioResponseDTO> comentar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CriarComentarioDTO dto) {

        ComentarioResponseDTO comentario = comentarioService.comentar(id, dto, jwt.getSubject());

        return ResponseEntity.status(HttpStatus.CREATED).body(comentario);
    }


    @GetMapping("/{id}/comentarios")
    public ResponseEntity<List<ComentarioResponseDTO>> listarComentarios(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(comentarioService.listarPorChamado(id, jwt.getSubject()));
    }
}