package com.example.biblioteca.repository;

import com.example.biblioteca.domain.Usuario;
import com.example.biblioteca.dto.UsuarioResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    
    Optional<Usuario> findByIdAndAtivoTrue(UUID id);

    List<UsuarioResponse> findAllByAtivoTrue();
}
