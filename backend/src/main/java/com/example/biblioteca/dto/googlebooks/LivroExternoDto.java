package com.example.biblioteca.dto.googlebooks;

import java.util.List;

public record LivroExternoDto(
    String id,
    String titulo,
    List<String> autores,
    String isbn,
    String categoria,
    String dataPublicacao,
    String thumbnailUrl
) {}
