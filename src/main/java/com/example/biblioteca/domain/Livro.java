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

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String autor;

    @Column(nullable = false, unique = true)
    private String isbn;

    @Column(name = "data_publicacao", nullable = false)
    private LocalDate dataPublicacao;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private boolean ativo;

    @Column(nullable = false)
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
