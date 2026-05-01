package com.example.biblioteca.dto;

import com.example.biblioteca.domain.Livro;
import java.time.LocalDate;
import java.util.UUID;

public record LivroResponse(
        UUID id,
        String titulo,
        String autor,
        String isbn,
        LocalDate dataPublicacao,
        String categoria,
        boolean disponivel
) {
    public LivroResponse(Livro livro) {
        this(
                livro.getId(),
                livro.getTitulo(),
                livro.getAutor(),
                livro.getIsbn(),
                livro.getDataPublicacao(),
                livro.getCategoria(),
                livro.isDisponivel()
        );
    }
}
