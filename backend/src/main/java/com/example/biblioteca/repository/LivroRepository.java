package com.example.biblioteca.repository;

import com.example.biblioteca.domain.Livro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LivroRepository extends JpaRepository<Livro, UUID> {

    Page<Livro> findAllByAtivoTrue(Pageable pageable);

    Optional<Livro> findByIdAndAtivoTrue(UUID id);

    @Query("""
            SELECT l FROM Livro l
            WHERE l.ativo = true AND l.disponivel = true
            AND l.categoria IN (SELECT DISTINCT el.categoria FROM Emprestimo e JOIN e.livro el WHERE e.usuario.id = :usuarioId)
            AND l.id NOT IN (SELECT e.livro.id FROM Emprestimo e WHERE e.usuario.id = :usuarioId)
    """)
    List<Livro> recomendarLivrosParaUsuario(@Param("usuarioId") UUID usuarioId);

    List<Livro> findTop20ByTituloContainingIgnoreCaseAndAtivoTrue(String titulo);

    boolean existsByIsbnIgnoreCase(String isbn);

    boolean existsByIsbnIgnoreCaseAndIdNot(String isbn, UUID id);
}
