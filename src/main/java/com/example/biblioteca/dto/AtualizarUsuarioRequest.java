package com.example.biblioteca.dto;

import lombok.NonNull;

import java.util.UUID;

public record AtualizarUsuarioRequest(
        @NonNull UUID id,
        String nome,
        String email,
        String telefone
) {
}
