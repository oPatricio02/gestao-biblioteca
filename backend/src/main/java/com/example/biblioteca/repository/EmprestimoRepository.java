package com.example.biblioteca.repository;

import com.example.biblioteca.domain.Emprestimo;
import com.example.biblioteca.enums.StatusEmprestimo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmprestimoRepository extends JpaRepository<Emprestimo, UUID> {
    
    List<Emprestimo> findByStatusAndDataDevolucaoBefore(StatusEmprestimo status, LocalDate data);

    boolean existsByUsuarioIdAndStatusIn(UUID usuarioId, Collection<StatusEmprestimo> status);

    boolean existsByLivroIdAndStatusIn(UUID livroId, Collection<StatusEmprestimo> status);

    boolean existsByLivroIdAndStatusInAndIdNot(UUID livroId, Collection<StatusEmprestimo> status, UUID id);
}
