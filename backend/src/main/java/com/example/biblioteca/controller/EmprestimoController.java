package com.example.biblioteca.controller;

import com.example.biblioteca.dto.AtualizarEmprestimoRequest;
import com.example.biblioteca.dto.CriarEmprestimoRequest;
import com.example.biblioteca.dto.EmprestimoResponse;
import com.example.biblioteca.services.EmprestimoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/emprestimos")
@RequiredArgsConstructor
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    @PostMapping
    public ResponseEntity<EmprestimoResponse> criar(@Validated @RequestBody CriarEmprestimoRequest request) {
        return emprestimoService.criar(request);
    }

    @GetMapping
    public ResponseEntity<Page<EmprestimoResponse>> listar(@PageableDefault Pageable pageable) {
        return ResponseEntity.ok(emprestimoService.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmprestimoResponse> obter(@PathVariable UUID id) {
        return emprestimoService.obter(id);
    }

    @PatchMapping
    public ResponseEntity<EmprestimoResponse> alterar(@Validated @RequestBody AtualizarEmprestimoRequest request) {
        return emprestimoService.alterar(request);
    }
}
