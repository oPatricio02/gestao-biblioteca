package com.example.biblioteca.repository;

import com.example.biblioteca.domain.Emprestimo;
import com.example.biblioteca.enums.StatusEmprestimo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmprestimoRepository extends JpaRepository<Emprestimo, UUID> {

    boolean existsByLivroIdAndStatus(UUID livroId, StatusEmprestimo status);
    
    List<Emprestimo> findByStatusAndDataDevolucaoBefore(StatusEmprestimo status, LocalDate data);
}
