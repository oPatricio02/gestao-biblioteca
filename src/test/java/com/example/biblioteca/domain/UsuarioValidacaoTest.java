package com.example.biblioteca.domain;

import com.example.biblioteca.dto.CriarUsuarioRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioValidacaoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void deveFalharQuandoCamposDoDtoSaoNulosVaziosOuEmailInvalido() {
        CriarUsuarioRequest requestInvalido = new CriarUsuarioRequest("", "email-invalido", null);
        
        Set<ConstraintViolation<CriarUsuarioRequest>> violations = validator.validate(requestInvalido);
        
        assertFalse(violations.isEmpty(), "Deveria haver erros de validação para campos nulos/vazios e email inválido");
    }

    @Test
    void devePassarQuandoDtoEhValido() {
        CriarUsuarioRequest requestValido = new CriarUsuarioRequest("João Silva", "joao@email.com", "11999999999");
        
        Set<ConstraintViolation<CriarUsuarioRequest>> violations = validator.validate(requestValido);
        
        assertTrue(violations.isEmpty(), "Não deveria haver erros de validação para um DTO válido");
    }

    @Test
    void deveFalharQuandoDataCadastroForNoFuturo() {
        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("João Silva")
                .email("joao@email.com")
                .telefone("11999999999")
                .ativo(true)
                .dataCadastro(LocalDateTime.now().plusDays(1)) // Data no futuro
                .build();

        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario);

        assertFalse(violations.isEmpty(), "Deveria falhar pois a data de cadastro está no futuro");
        
        boolean encontrouErroDeData = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("dataCadastro"));
        assertTrue(encontrouErroDeData, "O erro deve ser no campo dataCadastro");
    }

    @Test
    void devePassarQuandoDataCadastroForNoPassadoOuPresente() {
        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("João Silva")
                .email("joao@email.com")
                .telefone("11999999999")
                .ativo(true)
                .dataCadastro(LocalDateTime.now()) // Data atual
                .build();

        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario);

        assertTrue(violations.isEmpty(), "Não deveria haver erros pois a data de cadastro é o dia atual");
    }
}