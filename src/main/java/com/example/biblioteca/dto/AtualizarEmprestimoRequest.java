package com.example.biblioteca.dto;

import com.example.biblioteca.enums.StatusEmprestimo;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record AtualizarEmprestimoRequest(
        @NotNull
        UUID id,
        LocalDate dataDevolucao,
        StatusEmprestimo status
) {
}