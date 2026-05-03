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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;

@Service
@RequiredArgsConstructor
public class EmprestimoService {

    private static final EnumSet<StatusEmprestimo> STATUS_EMPRESTIMO_ABERTO =
            EnumSet.of(StatusEmprestimo.ATIVO, StatusEmprestimo.ATRASADO);

    private final EmprestimoRepository emprestimoRepository;
    private final UsuarioRepository usuarioRepository;
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
        Usuario usuario = usuarioRepository.findByIdAndAtivoTrue(request.usuarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado ou inativo"));
        Livro livro = livroService.obterLivro(request.livroId());

        if (!livro.isDisponivel() || existeOutroEmprestimoAberto(livro.getId(), null)) {
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

    public Page<EmprestimoResponse> listar(Pageable pageable) {
        return emprestimoRepository.findAll(pageable).map(EmprestimoResponse::new);
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
        StatusEmprestimo statusAnterior = emprestimo.getStatus();
        emprestimo.atualizar(request);

        if (request.status() != null) {
            sincronizarDisponibilidadeDoLivro(emprestimo, statusAnterior, request.status());
        }

        Emprestimo atualizado = emprestimoRepository.save(emprestimo);
        return ResponseEntity.ok(new EmprestimoResponse(atualizado));
    }

    public boolean usuarioPossuiEmprestimosAtivos(UUID usuarioId) {
        return emprestimoRepository.existsByUsuarioIdAndStatusIn(usuarioId, STATUS_EMPRESTIMO_ABERTO);
    }

    private void sincronizarDisponibilidadeDoLivro(Emprestimo emprestimo,
                                                   StatusEmprestimo statusAnterior,
                                                   StatusEmprestimo novoStatus) {
        Livro livro = emprestimo.getLivro();

        if (novoStatus == StatusEmprestimo.DEVOLVIDO) {
            livroService.atualizarDisponibilidade(livro, true);
            return;
        }

        if (STATUS_EMPRESTIMO_ABERTO.contains(novoStatus)) {
            if (statusAnterior == StatusEmprestimo.DEVOLVIDO && existeOutroEmprestimoAberto(livro.getId(), emprestimo.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Não é possível reabrir este empréstimo porque o livro já possui outro empréstimo em aberto");
            }

            livroService.atualizarDisponibilidade(livro, false);
        }
    }

    private boolean existeOutroEmprestimoAberto(UUID livroId, UUID emprestimoIdIgnorado) {
        if (emprestimoIdIgnorado == null) {
            return emprestimoRepository.existsByLivroIdAndStatusIn(livroId, STATUS_EMPRESTIMO_ABERTO);
        }

        return emprestimoRepository.existsByLivroIdAndStatusInAndIdNot(livroId, STATUS_EMPRESTIMO_ABERTO, emprestimoIdIgnorado);
    }
}
