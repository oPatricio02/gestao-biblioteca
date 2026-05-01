package com.example.biblioteca.domain;

import com.example.biblioteca.dto.AtualizarEmprestimoRequest;
import com.example.biblioteca.enums.StatusEmprestimo;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "emprestimos")
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @PastOrPresent
    @Column(name = "data_emprestimo", nullable = false)
    private LocalDate dataEmprestimo;

    @Column(name = "data_devolucao", nullable = false)
    private LocalDate dataDevolucao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEmprestimo status;
    
    public void atualizar(AtualizarEmprestimoRequest request) {
        if (request.status() != null) {
            this.status = request.status();
        }
        if (request.dataDevolucao() != null) {
            this.dataDevolucao = request.dataDevolucao();
        }
    }
}
