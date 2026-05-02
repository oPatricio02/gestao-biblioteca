package com.example.biblioteca.repository;

import com.example.biblioteca.domain.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LivroRepository extends JpaRepository<Livro, UUID> {

    org.springframework.data.domain.Page<Livro> findAllByAtivoTrue(org.springframework.data.domain.Pageable pageable);

    Optional<Livro> findByIdAndAtivoTrue(UUID id);

    @Query("""
            SELECT l FROM Livro l
            WHERE l.ativo = true AND l.disponivel = true
            AND l.categoria IN (SELECT DISTINCT el.categoria FROM Emprestimo e JOIN e.livro el WHERE e.usuario.id = :usuarioId)
            AND l.id NOT IN (SELECT e.livro.id FROM Emprestimo e WHERE e.usuario.id = :usuarioId)
    """)
    List<Livro> recomendarLivrosParaUsuario(@org.springframework.data.repository.query.Param("usuarioId") UUID usuarioId);
}
