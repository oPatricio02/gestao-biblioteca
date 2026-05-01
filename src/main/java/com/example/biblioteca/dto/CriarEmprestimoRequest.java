package com.example.biblioteca.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CriarEmprestimoRequest(
        @NotNull
        UUID usuarioId,
        @NotNull
        UUID livroId,
        @NotNull
        LocalDate dataDevolucao
) {
}