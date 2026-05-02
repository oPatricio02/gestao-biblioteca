package com.example.biblioteca.services;

import com.example.biblioteca.domain.Livro;
import com.example.biblioteca.dto.AtualizarLivroRequest;
import com.example.biblioteca.dto.CriarLivroRequest;
import com.example.biblioteca.dto.LivroResponse;
import com.example.biblioteca.repository.LivroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LivroService {

    private final LivroRepository livroRepository;

    public LivroResponse criar(CriarLivroRequest request) {
        Livro livro = Livro.builder()
                .titulo(request.titulo())
                .autor(request.autor())
                .isbn(request.isbn())
                .dataPublicacao(request.dataPublicacao())
                .categoria(request.categoria())
                .ativo(true)
                .disponivel(true)
                .build();

        return new LivroResponse(livroRepository.save(livro));
    }

    public org.springframework.data.domain.Page<LivroResponse> listar(org.springframework.data.domain.Pageable pageable) {
        return livroRepository.findAllByAtivoTrue(pageable).map(LivroResponse::new);
    }

    public List<LivroResponse> recomendarLivrosParaUsuario(UUID usuarioId) {
        return livroRepository.recomendarLivrosParaUsuario(usuarioId).stream()
                .map(LivroResponse::new)
                .collect(Collectors.toList());
    }

    public ResponseEntity<LivroResponse> obter(UUID id) {
        return livroRepository.findByIdAndAtivoTrue(id)
                .map(livro -> ResponseEntity.ok(new LivroResponse(livro)))
                .orElse(ResponseEntity.notFound().build());
    }

    public void deletar(UUID id) {
        livroRepository.findByIdAndAtivoTrue(id).ifPresent(livro -> {
            livro.setAtivo(false);
            livroRepository.save(livro);
        });
    }

    public Livro obterLivro(UUID id) {
        return livroRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Livro não encontrado ou inativo"));
    }

    public void atualizarDisponibilidade(Livro livro, boolean disponivel) {
        livro.setDisponivel(disponivel);
        livroRepository.save(livro);
    }

    public ResponseEntity<LivroResponse> alterar(AtualizarLivroRequest request) {
        Optional<Livro> livroOpt = livroRepository.findByIdAndAtivoTrue(request.id());

        if (livroOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Livro livro = livroOpt.get();
        livro.atualizar(request);
        livroRepository.save(livro);

        return ResponseEntity.ok(new LivroResponse(livro));
    }

    public List<LivroResponse> buscarPorTitulo(String titulo) {
        return livroRepository.findTop20ByTituloContainingIgnoreCaseAndAtivoTrue(titulo)
                .stream()
                .map(LivroResponse::new)
                .toList();
    }
}
