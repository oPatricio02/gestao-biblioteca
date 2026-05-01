package com.example.biblioteca.services;

import com.example.biblioteca.domain.Livro;
import com.example.biblioteca.dto.AtualizarLivroRequest;
import com.example.biblioteca.dto.CriarLivroRequest;
import com.example.biblioteca.dto.LivroResponse;
import com.example.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivroServiceTest {

    @Mock
    private LivroRepository livroRepository;

    @InjectMocks
    private LivroService livroService;

    @Test
    void testCriarLivro() {
        CriarLivroRequest request = new CriarLivroRequest("Titulo", "Autor", "ISBN", LocalDate.now(), "Categoria");
        Livro livro = new Livro(UUID.randomUUID(), "Titulo", "Autor", "ISBN", LocalDate.now(), "Categoria", true, true);
        
        when(livroRepository.save(any(Livro.class))).thenReturn(livro);

        LivroResponse response = livroService.criar(request);

        assertNotNull(response);
        assertEquals("Titulo", response.titulo());
        verify(livroRepository, times(1)).save(any(Livro.class));
    }

    @Test
    void testListar() {
        Livro livro = new Livro(UUID.randomUUID(), "Titulo", "Autor", "ISBN", LocalDate.now(), "Categoria", true, true);
        when(livroRepository.findAllByAtivoTrue()).thenReturn(List.of(livro));

        List<LivroResponse> list = livroService.listar();

        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
        verify(livroRepository, times(1)).findAllByAtivoTrue();
    }

    @Test
    void testObterEncontrado() {
        UUID id = UUID.randomUUID();
        Livro livro = new Livro(id, "Titulo", "Autor", "ISBN", LocalDate.now(), "Categoria", true, true);
        when(livroRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(livro));

        var response = livroService.obter(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Titulo", response.getBody().titulo());
    }

    @Test
    void testObterNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(livroRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.empty());

        var response = livroService.obter(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeletar() {
        UUID id = UUID.randomUUID();
        Livro livro = new Livro(id, "Titulo", "Autor", "ISBN", LocalDate.now(), "Categoria", true, true);
        when(livroRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(livro));

        livroService.deletar(id);

        assertFalse(livro.isAtivo());
        verify(livroRepository, times(1)).save(livro);
    }

    @Test
    void testAlterarEncontrado() {
        UUID id = UUID.randomUUID();
        AtualizarLivroRequest request = new AtualizarLivroRequest(id, "Titulo Atualizado", "Autor", "ISBN", LocalDate.now(), "Categoria");
        Livro livro = new Livro(id, "Titulo", "Autor", "ISBN", LocalDate.now(), "Categoria", true, true);
        
        when(livroRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(livro));

        var response = livroService.alterar(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Titulo Atualizado", livro.getTitulo());
        verify(livroRepository, times(1)).save(livro);
    }

    @Test
    void testRecomendarLivrosParaUsuario() {
        UUID usuarioId = UUID.randomUUID();
        Livro livro1 = new Livro(UUID.randomUUID(), "Titulo 1", "Autor 1", "ISBN 1", LocalDate.now(), "Ficção", true, true);
        Livro livro2 = new Livro(UUID.randomUUID(), "Titulo 2", "Autor 2", "ISBN 2", LocalDate.now(), "Ficção", true, true);
        
        when(livroRepository.recomendarLivrosParaUsuario(usuarioId)).thenReturn(List.of(livro1, livro2));

        List<LivroResponse> recomendacoes = livroService.recomendarLivrosParaUsuario(usuarioId);

        assertFalse(recomendacoes.isEmpty());
        assertEquals(2, recomendacoes.size());
        verify(livroRepository, times(1)).recomendarLivrosParaUsuario(usuarioId);
    }
}