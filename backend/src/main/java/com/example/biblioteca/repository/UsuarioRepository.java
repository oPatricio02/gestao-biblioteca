package com.example.biblioteca.repository;

import com.example.biblioteca.domain.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    
    Optional<Usuario> findByIdAndAtivoTrue(UUID id);

    Page<Usuario> findAllByAtivoTrue(Pageable pageable);

    List<Usuario> findTop20ByNomeContainingIgnoreCaseAndAtivoTrue(String nome);

    boolean existsByEmail(String email);
}
