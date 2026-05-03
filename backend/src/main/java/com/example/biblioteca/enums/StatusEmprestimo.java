package com.example.biblioteca.enums;

import lombok.Getter;

import java.util.EnumSet;

@Getter
public enum StatusEmprestimo {

    DEVOLVIDO(0),
    ATIVO(1),
    ATRASADO(2);

    public static final EnumSet<StatusEmprestimo> STATUS_ABERTOS =
            EnumSet.of(StatusEmprestimo.ATIVO, StatusEmprestimo.ATRASADO);

    private final int status;

    StatusEmprestimo(int status) {
        this.status = status;
    }
}
