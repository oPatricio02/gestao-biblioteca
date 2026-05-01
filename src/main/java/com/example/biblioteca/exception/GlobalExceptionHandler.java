package com.example.biblioteca.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<List<ErroDeValidacao>> tratarErroDeValidacao(MethodArgumentNotValidException ex) {
        List<ErroDeValidacao> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErroDeValidacao(erro.getField(), erro.getDefaultMessage()))
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(erros);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<List<ErroDeValidacao>> tratarErroDeValidacaoDeEntidade(ConstraintViolationException ex) {
        List<ErroDeValidacao> erros = ex.getConstraintViolations().stream()
                .map(erro -> new ErroDeValidacao(erro.getPropertyPath().toString(), erro.getMessage()))
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(erros);
    }
}