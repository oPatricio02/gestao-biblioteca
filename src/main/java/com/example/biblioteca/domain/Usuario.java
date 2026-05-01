package com.example.biblioteca.domain;

import com.example.biblioteca.dto.AtualizarUsuarioRequest;
import jakarta.persistence.*;
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

    private String nome;

    private String email;

    private LocalDateTime dataCadastro;

    private String telefone;

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
