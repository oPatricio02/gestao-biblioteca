package com.example.biblioteca.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "livros")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String titulo;

    private String autor;

    private String isbn;

    private LocalDate dataPublicacao;

    private String categoria;

    private boolean ativo;

    private boolean disponivel;

    public void atualizar(com.example.biblioteca.dto.AtualizarLivroRequest request) {
        if (request.titulo() != null) {
            this.titulo = request.titulo();
        }
        if (request.autor() != null) {
            this.autor = request.autor();
        }
        if (request.isbn() != null) {
            this.isbn = request.isbn();
        }
        if (request.dataPublicacao() != null) {
            this.dataPublicacao = request.dataPublicacao();
        }
        if (request.categoria() != null) {
            this.categoria = request.categoria();
        }
    }
}
