package com.example.biblioteca.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.UUID;

public record CriarEmprestimoRequest(
        @NotNull
        UUID usuarioId,
        @NotNull
        UUID livroId,
        @PastOrPresent(message = "A data de empréstimo não pode estar no futuro")
        LocalDate dataEmprestimo,
        @NotNull
        LocalDate dataDevolucao
) {
}