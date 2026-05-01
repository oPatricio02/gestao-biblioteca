package com.example.biblioteca.controller;

import com.example.biblioteca.dto.AtualizarLivroRequest;
import com.example.biblioteca.dto.CriarLivroRequest;
import com.example.biblioteca.dto.LivroResponse;
import com.example.biblioteca.services.LivroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/livros")
@RequiredArgsConstructor
public class LivroController {

    private final LivroService livroService;

    @PostMapping
    public ResponseEntity<LivroResponse> criar(@Validated @RequestBody CriarLivroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(livroService.criar(request));
    }

    @GetMapping
    public ResponseEntity<List<LivroResponse>> listar() {
        return ResponseEntity.ok(livroService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LivroResponse> obter(@PathVariable UUID id) {
        return livroService.obter(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        livroService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    public ResponseEntity<LivroResponse> alterar(@Validated @RequestBody AtualizarLivroRequest request) {
        return livroService.alterar(request);
    }
}
