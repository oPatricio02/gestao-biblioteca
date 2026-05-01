package com.example.biblioteca.domain;

import com.example.biblioteca.dto.AtualizarUsuarioRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @PastOrPresent
    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    @Column(nullable = false)
    private String telefone;

    @Column(nullable = false)
    private boolean ativo;

    public void atualizar(AtualizarUsuarioRequest atualizar){
        if(atualizar.email() != null){
            this.setEmail(atualizar.email());
        }
        if(atualizar.nome() != null){
            this.setNome(atualizar.nome());
        }
        if(atualizar.telefone() != null){
            this.setTelefone(atualizar.telefone());
        }
    }
}
