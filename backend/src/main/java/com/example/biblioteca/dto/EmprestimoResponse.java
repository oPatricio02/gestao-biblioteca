package com.example.biblioteca.dto;

import com.example.biblioteca.domain.Emprestimo;
import com.example.biblioteca.enums.StatusEmprestimo;

import java.time.LocalDate;
import java.util.UUID;

public record EmprestimoResponse(
        UUID id,
        UUID usuarioId,
        String nomeUsuario,
        UUID livroId,
        String tituloLivro,
        LocalDate dataEmprestimo,
        LocalDate dataDevolucao,
        StatusEmprestimo status
) {
    public EmprestimoResponse(Emprestimo emprestimo) {
        this(
                emprestimo.getId(),
                emprestimo.getUsuario().getId(),
                emprestimo.getUsuario().getNome(),
                emprestimo.getLivro().getId(),
                emprestimo.getLivro().getTitulo(),
                emprestimo.getDataEmprestimo(),
                emprestimo.getDataDevolucao(),
                emprestimo.getStatus()
        );
    }
}