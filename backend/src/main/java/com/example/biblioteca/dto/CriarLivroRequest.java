package com.example.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CriarLivroRequest(
        @NotBlank
        String titulo,
        @NotBlank
        String autor,
        @NotBlank
        String isbn,
        @NotNull
        LocalDate dataPublicacao,
        @NotBlank
        String categoria
) {
}
