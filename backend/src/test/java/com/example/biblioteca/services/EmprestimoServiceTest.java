package com.example.biblioteca.services;

import com.example.biblioteca.domain.Emprestimo;
import com.example.biblioteca.domain.Livro;
import com.example.biblioteca.domain.Usuario;
import com.example.biblioteca.dto.AtualizarEmprestimoRequest;
import com.example.biblioteca.dto.CriarEmprestimoRequest;
import com.example.biblioteca.dto.EmprestimoResponse;
import com.example.biblioteca.enums.StatusEmprestimo;
import com.example.biblioteca.repository.EmprestimoRepository;
import com.example.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmprestimoServiceTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private LivroService livroService;

    @InjectMocks
    private EmprestimoService emprestimoService;

    @Test
    void deveCriarEmprestimoComSucesso() {
        UUID usuarioId = UUID.randomUUID();
        UUID livroId = UUID.randomUUID();
        
        Usuario usuario = new Usuario(usuarioId, "João", "joao@email.com", null, "11999999999", true);
        Livro livro = new Livro(livroId, "O Hobbit", "Tolkien", "1234", LocalDate.now(), "Fantasia", true, true);
        
        CriarEmprestimoRequest request = new CriarEmprestimoRequest(usuarioId, livroId, null, LocalDate.now().plusDays(7));
        
        when(usuarioRepository.findByIdAndAtivoTrue(usuarioId)).thenReturn(Optional.of(usuario));
        when(livroService.obterLivro(livroId)).thenReturn(livro);
        when(emprestimoRepository.existsByLivroIdAndStatusIn(eq(livroId), any())).thenReturn(false);

        Emprestimo emprestimoSalvo = Emprestimo.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .livro(livro)
                .dataEmprestimo(LocalDate.now())
                .dataDevolucao(request.dataDevolucao())
                .status(StatusEmprestimo.ATIVO)
                .build();

        when(emprestimoRepository.save(any(Emprestimo.class))).thenReturn(emprestimoSalvo);

        ResponseEntity<EmprestimoResponse> response = emprestimoService.criar(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("O Hobbit", response.getBody().tituloLivro());
        
        verify(livroService, times(1)).atualizarDisponibilidade(livro, false);
        verify(emprestimoRepository, times(1)).save(any(Emprestimo.class));
    }

    @Test
    void deveFalharAoCriarEmprestimoQuandoUsuarioNaoEncontrado() {
        CriarEmprestimoRequest request = new CriarEmprestimoRequest(UUID.randomUUID(), UUID.randomUUID(), null, LocalDate.now().plusDays(7));
        
        when(usuarioRepository.findByIdAndAtivoTrue(request.usuarioId())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> emprestimoService.criar(request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assert exception.getReason() != null;
        assertTrue(exception.getReason().contains("Usuário não encontrado"));
    }

    @Test
    void deveFalharAoCriarEmprestimoQuandoLivroNaoDisponivel() {
        UUID usuarioId = UUID.randomUUID();
        UUID livroId = UUID.randomUUID();
        
        Usuario usuario = new Usuario(usuarioId, "João", "joao@email.com", null, "11999999999", true);
        Livro livro = new Livro(livroId, "O Hobbit", "Tolkien", "1234", LocalDate.now(), "Fantasia", true, false);
        
        CriarEmprestimoRequest request = new CriarEmprestimoRequest(usuarioId, livroId, null, LocalDate.now().plusDays(7));
        
        when(usuarioRepository.findByIdAndAtivoTrue(usuarioId)).thenReturn(Optional.of(usuario));
        when(livroService.obterLivro(livroId)).thenReturn(livro);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> emprestimoService.criar(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assert exception.getReason() != null;
        assertTrue(exception.getReason().contains("já está emprestado"));
    }

    @Test
    void deveAtualizarStatusParaDevolvidoELiberarLivro() {
        UUID id = UUID.randomUUID();
        Livro livro = new Livro(UUID.randomUUID(), "O Hobbit", "Tolkien", "1234", LocalDate.now(), "Fantasia", true, false);
        Usuario usuario = new Usuario(UUID.randomUUID(), "João", "joao@email.com", null, "11999999999", true);
        
        Emprestimo emprestimo = new Emprestimo(id, usuario, livro, LocalDate.now().minusDays(5), LocalDate.now().plusDays(2), StatusEmprestimo.ATIVO);
        
        AtualizarEmprestimoRequest request = new AtualizarEmprestimoRequest(id, LocalDate.now(), StatusEmprestimo.DEVOLVIDO);

        when(emprestimoRepository.findById(id)).thenReturn(Optional.of(emprestimo));
        when(emprestimoRepository.save(any(Emprestimo.class))).thenReturn(emprestimo);

        ResponseEntity<EmprestimoResponse> response = emprestimoService.alterar(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assert response.getBody() != null;
        assertEquals(StatusEmprestimo.DEVOLVIDO, response.getBody().status());

        verify(livroService, times(1)).atualizarDisponibilidade(livro, true);
        verify(emprestimoRepository, times(1)).save(emprestimo);
    }

    @Test
    void deveImpedirReaberturaQuandoJaExisteOutroEmprestimoAbertoParaOLivro() {
        UUID id = UUID.randomUUID();
        UUID livroId = UUID.randomUUID();
        Livro livro = new Livro(livroId, "O Hobbit", "Tolkien", "1234", LocalDate.now(), "Fantasia", true, true);
        Usuario usuario = new Usuario(UUID.randomUUID(), "João", "joao@email.com", null, "11999999999", true);

        Emprestimo emprestimo = new Emprestimo(id, usuario, livro, LocalDate.now().minusDays(5), LocalDate.now(), StatusEmprestimo.DEVOLVIDO);
        AtualizarEmprestimoRequest request = new AtualizarEmprestimoRequest(id, LocalDate.now().plusDays(7), StatusEmprestimo.ATIVO);

        when(emprestimoRepository.findById(id)).thenReturn(Optional.of(emprestimo));
        when(emprestimoRepository.existsByLivroIdAndStatusInAndIdNot(eq(livroId), any(), eq(id))).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> emprestimoService.alterar(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(emprestimoRepository, never()).save(any(Emprestimo.class));
    }

    @Test
    void deveAtualizarEmprestimosAtrasados() {
        Livro livro = new Livro(UUID.randomUUID(), "O Hobbit", "Tolkien", "1234", LocalDate.now(), "Fantasia", true, false);
        Usuario usuario = new Usuario(UUID.randomUUID(), "João", "joao@email.com", null, "11999999999", true);
        
        Emprestimo emp1 = new Emprestimo(UUID.randomUUID(), usuario, livro, LocalDate.now().minusDays(10), LocalDate.now().minusDays(1), StatusEmprestimo.ATIVO);
        Emprestimo emp2 = new Emprestimo(UUID.randomUUID(), usuario, livro, LocalDate.now().minusDays(5), LocalDate.now().minusDays(2), StatusEmprestimo.ATIVO);
        
        List<Emprestimo> emprestimos = List.of(emp1, emp2);
        
        when(emprestimoRepository.findByStatusAndDataDevolucaoBefore(eq(StatusEmprestimo.ATIVO), any(LocalDate.class))).thenReturn(emprestimos);

        emprestimoService.atualizarEmprestimosAtrasados();

        assertEquals(StatusEmprestimo.ATRASADO, emp1.getStatus());
        assertEquals(StatusEmprestimo.ATRASADO, emp2.getStatus());
        verify(emprestimoRepository, times(1)).saveAll(emprestimos);
    }

    @Test
    void deveInformarQuandoUsuarioPossuiEmprestimosAtivos() {
        UUID usuarioId = UUID.randomUUID();
        when(emprestimoRepository.existsByUsuarioIdAndStatusIn(eq(usuarioId), any())).thenReturn(true);

        boolean possuiEmprestimosAtivos = emprestimoService.usuarioPossuiEmprestimosAtivos(usuarioId);

        assertTrue(possuiEmprestimosAtivos);
        verify(emprestimoRepository, times(1)).existsByUsuarioIdAndStatusIn(eq(usuarioId), any());
    }
}
