package com.example.biblioteca.domain;

import com.example.biblioteca.dto.CriarLivroRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LivroValidacaoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void deveFalharQuandoCamposDoLivroForemNulosOuVazios() {
        CriarLivroRequest requestInvalido = new CriarLivroRequest("", "", null, null, "  ");
        
        Set<ConstraintViolation<CriarLivroRequest>> violations = validator.validate(requestInvalido);
        
        assertFalse(violations.isEmpty(), "Deveria haver erros de validação quando os campos são nulos ou vazios");
    }

    @Test
    void devePassarQuandoTodosOsCamposDoLivroForemValidos() {
        CriarLivroRequest requestValido = new CriarLivroRequest("O Senhor dos Anéis", "J.R.R. Tolkien", "1234567890", LocalDate.of(1954, 7, 29), "Fantasia");
        
        Set<ConstraintViolation<CriarLivroRequest>> violations = validator.validate(requestValido);
        
        assertTrue(violations.isEmpty(), "Não deveria haver erros de validação quando o livro está correto");
    }
}