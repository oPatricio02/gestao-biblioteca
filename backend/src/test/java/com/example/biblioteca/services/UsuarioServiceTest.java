package com.example.biblioteca.services;

import com.example.biblioteca.domain.Usuario;
import com.example.biblioteca.dto.AtualizarUsuarioRequest;
import com.example.biblioteca.dto.CriarUsuarioRequest;
import com.example.biblioteca.dto.UsuarioResponse;
import com.example.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

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

    @InjectMocks
    private UsuarioService usuarioService;

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
        UsuarioResponse response = new UsuarioResponse(UUID.randomUUID(), "Teste Teste", "teste@teste.com");
        when(usuarioRepository.findAllByAtivoTrue()).thenReturn(List.of(response));

        List<UsuarioResponse> list = usuarioService.listar();

        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
        verify(usuarioRepository, times(1)).findAllByAtivoTrue();
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

        usuarioService.deletar(id);

        assertFalse(usuario.isAtivo());
        verify(usuarioRepository, times(1)).save(usuario);
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