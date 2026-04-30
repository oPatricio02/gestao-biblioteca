package com.example.biblioteca.enums;

import lombok.Getter;

@Getter
public enum StatusEmprestimo {

    DEVOLVIDO(0),
    ATIVO(1),
    ATRASADO(2);

    private int status;

    StatusEmprestimo(int status) {
        this.status = status;
    }
}
