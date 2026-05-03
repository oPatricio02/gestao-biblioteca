package com.example.biblioteca.services;

import com.example.biblioteca.domain.Usuario;
import com.example.biblioteca.dto.AtualizarUsuarioRequest;
import com.example.biblioteca.dto.CriarUsuarioRequest;
import com.example.biblioteca.dto.UsuarioResponse;
import com.example.biblioteca.repository.EmprestimoRepository;
import com.example.biblioteca.repository.UsuarioRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void testCriarUsuarioComEmailJaExistente() {
        CriarUsuarioRequest request = new CriarUsuarioRequest("Teste", "teste@teste.com", "123456789");
        when(usuarioRepository.existsByEmail("teste@teste.com")).thenReturn(true);

        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.criarUsuario(request);
        });

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void testCriarUsuario() {
        CriarUsuarioRequest request = new CriarUsuarioRequest("Teste Teste", "teste@teste.com", "123456789");
        Usuario usuario = new Usuario(UUID.randomUUID(), "Teste Teste", "teste@teste.com", null, "123456789", true);
        
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        UsuarioResponse response = usuarioService.criarUsuario(request);

        assertNotNull(response);
        assertEquals("Teste Teste", response.nome());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void testListar() {
        Usuario user = new Usuario(UUID.randomUUID(), "Teste Teste", "teste@teste.com", null, "123456789", true);
        Page<Usuario> page = new PageImpl<>(List.of(user));
        Pageable pageable = PageRequest.of(0, 10);

        when(usuarioRepository.findAllByAtivoTrue(pageable)).thenReturn(page);

        Page<UsuarioResponse> result = usuarioService.listar(pageable);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(usuarioRepository, times(1)).findAllByAtivoTrue(pageable);
    }

    @Test
    void testObterEncontrado() {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario(id, "Teste Teste", "teste@teste.com", null, "123456789", true);
        when(usuarioRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(usuario));

        var response = usuarioService.obter(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Teste Teste", response.getBody().nome());
    }

    @Test
    void testObterNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.empty());

        var response = usuarioService.obter(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeletar() {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario(id, "Teste Teste", "teste@teste.com", null, "123456789", true);
        when(usuarioRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(usuario));
        when(emprestimoRepository.existsByUsuarioIdAndStatusIn(eq(id), any())).thenReturn(false);

        usuarioService.deletar(id);

        assertFalse(usuario.isAtivo());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void testDeletarUsuarioComEmprestimoAtivo() {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario(id, "Teste Teste", "teste@teste.com", null, "123456789", true);
        when(usuarioRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(usuario));
        when(emprestimoRepository.existsByUsuarioIdAndStatusIn(eq(id), any())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> usuarioService.deletar(id));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Usuário possui empréstimos ativos e não pode ser excluído", exception.getReason());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void testAlterarEncontrado() {
        UUID id = UUID.randomUUID();
        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest(id, "Teste Alterado", "teste@alterado.com", "987654321");
        Usuario usuario = new Usuario(id, "Teste Teste", "teste@teste.com", null, "123456789", true);
        
        when(usuarioRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(usuario));

        var response = usuarioService.alterar(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Teste Alterado", usuario.getNome());
        verify(usuarioRepository, times(1)).save(usuario);
    }
}