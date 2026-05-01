package com.example.biblioteca.services;

import com.example.biblioteca.domain.Usuario;
import com.example.biblioteca.dto.AtualizarUsuarioRequest;
import com.example.biblioteca.dto.CriarUsuarioRequest;
import com.example.biblioteca.dto.UsuarioResponse;
import com.example.biblioteca.dto.ObterUsuarioResponse;
import com.example.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioResponse criarUsuario(CriarUsuarioRequest request){
        var user = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .telefone(request.telefone())
                .ativo(true)
                .dataCadastro(LocalDateTime.now())
                .build();

        return new UsuarioResponse(usuarioRepository.save(user));
    }


    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAllByAtivoTrue();
    }

    public ResponseEntity<ObterUsuarioResponse> obter(UUID id) {
        return usuarioRepository.findByIdAndAtivoTrue(id)
                .map(usuario -> ResponseEntity.ok().body(new ObterUsuarioResponse(usuario)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public void deletar(UUID id) {
        usuarioRepository.findByIdAndAtivoTrue(id).ifPresent(usuario -> {
            usuario.setAtivo(false);
            usuarioRepository.save(usuario);
        });
    }

    public ResponseEntity<ObterUsuarioResponse> alterar(AtualizarUsuarioRequest request) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByIdAndAtivoTrue(request.id());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = usuarioOpt.get();
        usuario.atualizar(request);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(
                new ObterUsuarioResponse(usuario)
        );
    }
}
