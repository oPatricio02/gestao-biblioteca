package com.example.biblioteca.dto;

import com.example.biblioteca.domain.Usuario;

import java.time.LocalDateTime;
import java.util.UUID;

public record ObterUsuarioResponse(
        UUID id,
        String nome,
        String email,
        LocalDateTime dataCadastro,
        String telefone
) {
    public ObterUsuarioResponse(Usuario usuario){
        this(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getDataCadastro(), usuario.getTelefone());
    }

}
