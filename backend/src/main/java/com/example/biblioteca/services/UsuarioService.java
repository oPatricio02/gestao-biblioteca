package com.example.biblioteca.services;

import com.example.biblioteca.domain.Usuario;
import com.example.biblioteca.dto.AtualizarUsuarioRequest;
import com.example.biblioteca.dto.CriarUsuarioRequest;
import com.example.biblioteca.dto.UsuarioResponse;
import com.example.biblioteca.dto.ObterUsuarioResponse;
import com.example.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmprestimoService emprestimoService;

    public UsuarioResponse criarUsuario(CriarUsuarioRequest request){
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }

        var user = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .telefone(request.telefone())
                .ativo(true)
                .dataCadastro(LocalDateTime.now())
                .build();

        return new UsuarioResponse(usuarioRepository.save(user));
    }


    public Page<UsuarioResponse> listar(Pageable pageable) {
        return usuarioRepository.findAllByAtivoTrue(pageable).map(UsuarioResponse::new);
    }

    public ResponseEntity<ObterUsuarioResponse> obter(UUID id) {
        return usuarioRepository.findByIdAndAtivoTrue(id)
                .map(usuario -> ResponseEntity.ok().body(new ObterUsuarioResponse(usuario)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public void deletar(UUID id) {
        usuarioRepository.findByIdAndAtivoTrue(id).ifPresent(usuario -> {
            if (emprestimoService.usuarioPossuiEmprestimosAtivos(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Usuário possui empréstimos ativos e não pode ser excluído");
            }

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

        if (request.email() != null && !request.email().equals(usuario.getEmail()) && usuarioRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado por outro usuário");
        }

        usuario.atualizar(request);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(
                new ObterUsuarioResponse(usuario)
        );
    }

    public List<ObterUsuarioResponse> buscarPorNome(String nome) {
        return usuarioRepository.findTop20ByNomeContainingIgnoreCaseAndAtivoTrue(nome)
                .stream()
                .map(ObterUsuarioResponse::new)
                .toList();
    }
}
