package com.example.biblioteca.repository;

import com.example.biblioteca.domain.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LivroRepository extends JpaRepository<Livro, UUID> {

    List<Livro> findAllByAtivoTrue();

    Optional<Livro> findByIdAndAtivoTrue(UUID id);
}
