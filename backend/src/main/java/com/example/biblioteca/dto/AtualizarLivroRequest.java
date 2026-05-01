package com.example.biblioteca.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record AtualizarLivroRequest(
        @NotNull
        UUID id,
        String titulo,
        String autor,
        String isbn,
        LocalDate dataPublicacao,
        String categoria
) {
}
