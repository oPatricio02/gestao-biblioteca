package com.example.biblioteca.services;

import com.example.biblioteca.domain.Emprestimo;
import com.example.biblioteca.domain.Livro;
import com.example.biblioteca.domain.Usuario;
import com.example.biblioteca.dto.AtualizarEmprestimoRequest;
import com.example.biblioteca.dto.CriarEmprestimoRequest;
import com.example.biblioteca.dto.EmprestimoResponse;
import com.example.biblioteca.enums.StatusEmprestimo;
import com.example.biblioteca.repository.EmprestimoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;

@Service
@RequiredArgsConstructor
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final UsuarioService usuarioService;
    private final LivroService livroService;

    @Scheduled(cron = "0 0 0 * * ?", zone = "America/Sao_Paulo")
    public void atualizarEmprestimosAtrasados() {
        List<Emprestimo> emprestimosAtrasados = emprestimoRepository.findByStatusAndDataDevolucaoBefore(StatusEmprestimo.ATIVO, LocalDate.now());
        
        emprestimosAtrasados.forEach(emprestimo -> emprestimo.setStatus(StatusEmprestimo.ATRASADO));

        if (!emprestimosAtrasados.isEmpty()) {
            emprestimoRepository.saveAll(emprestimosAtrasados);
        }
    }

    public ResponseEntity<EmprestimoResponse> criar(CriarEmprestimoRequest request) {
        Usuario usuario = usuarioService.obterUsuario(request.usuarioId());
        Livro livro = livroService.obterLivro(request.livroId());

        if (!livro.isDisponivel()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O livro já está emprestado e não está disponível");
        }

        Emprestimo emprestimo = Emprestimo.builder()
                .usuario(usuario)
                .livro(livro)
                .dataEmprestimo(request.dataEmprestimo() != null ? request.dataEmprestimo() : LocalDate.now())
                .dataDevolucao(request.dataDevolucao())
                .status(StatusEmprestimo.ATIVO)
                .build();

        livroService.atualizarDisponibilidade(livro, false);

        Emprestimo salvo = emprestimoRepository.save(emprestimo);
        return ResponseEntity.status(HttpStatus.CREATED).body(new EmprestimoResponse(salvo));
    }

    public List<EmprestimoResponse> listar() {
        return emprestimoRepository.findAll().stream()
                .map(EmprestimoResponse::new)
                .collect(Collectors.toList());
    }

    public ResponseEntity<EmprestimoResponse> obter(UUID id) {
        return emprestimoRepository.findById(id)
                .map(emprestimo -> ResponseEntity.ok(new EmprestimoResponse(emprestimo)))
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<EmprestimoResponse> alterar(AtualizarEmprestimoRequest request) {
        Optional<Emprestimo> emprestimoOpt = emprestimoRepository.findById(request.id());

        if (emprestimoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Emprestimo emprestimo = emprestimoOpt.get();
        emprestimo.atualizar(request);

        if (request.status() != null && request.status() == StatusEmprestimo.DEVOLVIDO) {
            Livro livro = emprestimo.getLivro();
            livroService.atualizarDisponibilidade(livro, true);
        }

        Emprestimo atualizado = emprestimoRepository.save(emprestimo);
        return ResponseEntity.ok(new EmprestimoResponse(atualizado));
    }
}
