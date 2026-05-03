package com.example.biblioteca.services;

import com.example.biblioteca.domain.Livro;
import com.example.biblioteca.dto.AtualizarLivroRequest;
import com.example.biblioteca.dto.CriarLivroRequest;
import com.example.biblioteca.dto.LivroResponse;
import com.example.biblioteca.repository.LivroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LivroService {

    private final LivroRepository livroRepository;

    public LivroResponse criar(CriarLivroRequest request) {
        String isbnNormalizado = normalizarIsbn(request.isbn());
        validarIsbnDuplicado(isbnNormalizado, null);

        Livro livro = Livro.builder()
                .titulo(request.titulo())
                .autor(request.autor())
                .isbn(isbnNormalizado)
                .dataPublicacao(request.dataPublicacao())
                .categoria(request.categoria())
                .ativo(true)
                .disponivel(true)
                .build();

        return new LivroResponse(livroRepository.save(livro));
    }

    public List<LivroResponse> criarEmLote(List<CriarLivroRequest> requests) {
        validarLoteDeIsbns(requests);

        List<Livro> livros = requests.stream().map(request -> Livro.builder()
                .titulo(request.titulo())
                .autor(request.autor())
                .isbn(normalizarIsbn(request.isbn()))
                .dataPublicacao(request.dataPublicacao())
                .categoria(request.categoria())
                .ativo(true)
                .disponivel(true)
                .build()).collect(Collectors.toList());

        List<Livro> salvos = livroRepository.saveAll(livros);
        return salvos.stream().map(LivroResponse::new).collect(Collectors.toList());
    }

    public Page<LivroResponse> listar(Pageable pageable) {
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
            if (!livro.isDisponivel()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Livro possui empréstimos ativos e não pode ser excluído");
            }

            livro.setAtivo(false);
            livroRepository.save(livro);
        });
    }

    public Livro obterLivro(UUID id) {
        return livroRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado ou inativo"));
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

        if (request.isbn() != null) {
            String isbnNormalizado = normalizarIsbn(request.isbn());
            validarIsbnDuplicado(isbnNormalizado, livro.getId());
        }

        livro.atualizar(request);
        if (request.isbn() != null) {
            livro.setIsbn(normalizarIsbn(request.isbn()));
        }
        livroRepository.save(livro);

        return ResponseEntity.ok(new LivroResponse(livro));
    }

    public List<LivroResponse> buscarPorTitulo(String titulo) {
        return livroRepository.findTop20ByTituloContainingIgnoreCaseAndAtivoTrue(titulo)
                .stream()
                .map(LivroResponse::new)
                .toList();
    }

    private void validarLoteDeIsbns(List<CriarLivroRequest> requests) {
        Set<String> isbns = new HashSet<>();

        for (CriarLivroRequest request : requests) {
            String isbnNormalizado = normalizarIsbn(request.isbn());

            if (!isbns.add(isbnNormalizado)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A lista possui ISBNs duplicados");
            }

            validarIsbnDuplicado(isbnNormalizado, null);
        }
    }

    private void validarIsbnDuplicado(String isbn, UUID livroId) {
        boolean duplicado = livroId == null
                ? livroRepository.existsByIsbnIgnoreCase(isbn)
                : livroRepository.existsByIsbnIgnoreCaseAndIdNot(isbn, livroId);

        if (duplicado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um livro cadastrado com este ISBN");
        }
    }

    private String normalizarIsbn(String isbn) {
        return isbn.replaceAll("[^0-9Xx]", "").toUpperCase();
    }
}
