package com.gabriel.helpdesk.exception;

import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarErroValidacao(MethodArgumentNotValidException exception) {

        //Criar tabela de erros
        Map<String, String> erros = new HashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(erro -> {
            erros.put(erro.getField(), erro.getDefaultMessage());
        });

        //Criar tabela de resposta com base nos erros
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("Status", 400);
        resposta.put("erros", erros);

        //retorno da tabela de resposta
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);

    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> tratarJsonInvalido(HttpMessageNotReadableException exception) {

        Map<String, Object> resposta = new HashMap<>();

        resposta.put("status", 400);
        resposta.put("erro", "JSON invalido ou valor informado não é permitido");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);

    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> tratarRegraDeNegocio(IllegalArgumentException exception) {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("status", 400);
        resposta.put("erro", exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> tratarNaoEncontrado(RecursoNaoEncontradoException exception) {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("status", 404);
        resposta.put("erro", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, Object>> tratarRegraNegocio(RegraNegocioException exception) {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("status", 400);
        resposta.put("erro", exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<Map<String, Object>> tratarCredenciaisInvalidas(CredenciaisInvalidasException exception) {

        Map<String, Object> resposta = new HashMap<>();

        resposta.put("status", 401);
        resposta.put("erro", exception.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resposta);
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<Map<String, Object>> tratarAcessoNegado(AcessoNegadoException exception) {

        Map<String, Object> resposta = new HashMap<>();

        resposta.put("status", 403);
        resposta.put("erro", exception.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resposta);
    }

}
