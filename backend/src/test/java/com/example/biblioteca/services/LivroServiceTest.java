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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
        CriarLivroRequest request = new CriarLivroRequest("Titulo", "Autor", "9780306406157", LocalDate.now(), "Categoria");
        Livro livro = new Livro(UUID.randomUUID(), "Titulo", "Autor", "9780306406157", LocalDate.now(), "Categoria", true, true);
        
        when(livroRepository.existsByIsbnIgnoreCase("9780306406157")).thenReturn(false);
        when(livroRepository.save(any(Livro.class))).thenReturn(livro);

        LivroResponse response = livroService.criar(request);

        assertNotNull(response);
        assertEquals("Titulo", response.titulo());
        verify(livroRepository, times(1)).save(any(Livro.class));
    }

    @Test
    void testCriarEmLote() {
        CriarLivroRequest req1 = new CriarLivroRequest("T1", "A1", "9780306406157", LocalDate.now(), "C1");
        CriarLivroRequest req2 = new CriarLivroRequest("T2", "A2", "8535902775", LocalDate.now(), "C2");
        Livro l1 = new Livro(UUID.randomUUID(), "T1", "A1", "9780306406157", LocalDate.now(), "C1", true, true);
        Livro l2 = new Livro(UUID.randomUUID(), "T2", "A2", "8535902775", LocalDate.now(), "C2", true, true);

        when(livroRepository.existsByIsbnIgnoreCase("9780306406157")).thenReturn(false);
        when(livroRepository.existsByIsbnIgnoreCase("8535902775")).thenReturn(false);
        when(livroRepository.saveAll(anyList())).thenReturn(List.of(l1, l2));

        List<LivroResponse> response = livroService.criarEmLote(List.of(req1, req2));

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("T1", response.get(0).titulo());
        assertEquals("T2", response.get(1).titulo());
        verify(livroRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testListar() {
        Livro livro = new Livro(UUID.randomUUID(), "Titulo", "Autor", "ISBN", LocalDate.now(), "Categoria", true, true);
        Page<Livro> page = new PageImpl<>(List.of(livro));
        Pageable pageable = PageRequest.of(0, 10);
        
        when(livroRepository.findAllByAtivoTrue(pageable)).thenReturn(page);

        Page<LivroResponse> result = livroService.listar(pageable);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(livroRepository, times(1)).findAllByAtivoTrue(pageable);
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
    void testDeletarLivroComEmprestimoAtivo() {
        UUID id = UUID.randomUUID();
        Livro livro = new Livro(id, "Titulo", "Autor", "ISBN", LocalDate.now(), "Categoria", true, false);
        when(livroRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(livro));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> livroService.deletar(id));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Livro possui empréstimos ativos e não pode ser excluído", exception.getReason());
        verify(livroRepository, never()).save(any(Livro.class));
    }

    @Test
    void testAlterarEncontrado() {
        UUID id = UUID.randomUUID();
        AtualizarLivroRequest request = new AtualizarLivroRequest(id, "Titulo Atualizado", "Autor", "9780306406157", LocalDate.now(), "Categoria");
        Livro livro = new Livro(id, "Titulo", "Autor", "9780306406157", LocalDate.now(), "Categoria", true, true);
        
        when(livroRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(livro));
        when(livroRepository.existsByIsbnIgnoreCaseAndIdNot("9780306406157", id)).thenReturn(false);

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

    @Test
    void testCriarLivroComIsbnDuplicado() {
        CriarLivroRequest request = new CriarLivroRequest("Titulo", "Autor", "9780306406157", LocalDate.now(), "Categoria");
        when(livroRepository.existsByIsbnIgnoreCase("9780306406157")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> livroService.criar(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(livroRepository, never()).save(any(Livro.class));
    }

    @Test
    void testCriarEmLoteComIsbnDuplicadoNaLista() {
        CriarLivroRequest req1 = new CriarLivroRequest("T1", "A1", "9780306406157", LocalDate.now(), "C1");
        CriarLivroRequest req2 = new CriarLivroRequest("T2", "A2", "9780306406157", LocalDate.now(), "C2");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> livroService.criarEmLote(List.of(req1, req2)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(livroRepository, never()).saveAll(anyList());
    }
}
