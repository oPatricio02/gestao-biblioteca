package com.example.biblioteca.controller;

import com.example.biblioteca.dto.AtualizarUsuarioRequest;
import com.example.biblioteca.dto.CriarUsuarioRequest;
import com.example.biblioteca.dto.UsuarioResponse;
import com.example.biblioteca.dto.ObterUsuarioResponse;
import com.example.biblioteca.services.UsuarioService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping ("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    ResponseEntity<UsuarioResponse> create(@Validated @RequestBody CriarUsuarioRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.criarUsuario(request));
    }

    @GetMapping
    ResponseEntity<List<UsuarioResponse>> listar(){
        return ResponseEntity.ok().body(usuarioService.listar());
    }

    @GetMapping("/{id}")
    ResponseEntity<ObterUsuarioResponse> obter(@PathVariable("id") UUID id){
        return usuarioService.obter(id);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deletar(@PathVariable("id") UUID id){
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    ResponseEntity<ObterUsuarioResponse> alterar(@RequestBody AtualizarUsuarioRequest request){
        return usuarioService.alterar(request);
    }

}
