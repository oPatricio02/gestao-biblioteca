package com.example.biblioteca.dto;

import jakarta.validation.constraints.Email;
import lombok.NonNull;

import java.util.UUID;

public record AtualizarUsuarioRequest(
        @NonNull UUID id,
        String nome,
        @Email(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "formato de e-mail inválido")
        String email,
        String telefone
) {
}
