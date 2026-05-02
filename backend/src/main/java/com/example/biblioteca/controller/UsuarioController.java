package com.example.biblioteca.controller;

import com.example.biblioteca.dto.AtualizarUsuarioRequest;
import com.example.biblioteca.dto.CriarUsuarioRequest;
import com.example.biblioteca.dto.UsuarioResponse;
import com.example.biblioteca.dto.ObterUsuarioResponse;
import com.example.biblioteca.services.LivroService;
import com.example.biblioteca.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    private final LivroService livroService;

    @PostMapping
    ResponseEntity<UsuarioResponse> create(@Validated @RequestBody CriarUsuarioRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.criarUsuario(request));
    }

    @GetMapping
    ResponseEntity<Page<UsuarioResponse>> listar(@PageableDefault Pageable pageable){
        return ResponseEntity.ok().body(usuarioService.listar(pageable));
    }

    @GetMapping("/{id}")
    ResponseEntity<ObterUsuarioResponse> obter(@PathVariable("id") UUID id){
        return usuarioService.obter(id);
    }

    @GetMapping("/{id}/recomendacoes")
    ResponseEntity<List<com.example.biblioteca.dto.LivroResponse>> recomendarLivros(@PathVariable("id") UUID id){
        return ResponseEntity.ok(livroService.recomendarLivrosParaUsuario(id));
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

    @GetMapping("/buscar")
    ResponseEntity<List<ObterUsuarioResponse>> buscar(@RequestParam("nome") String nome) {
        return ResponseEntity.ok(usuarioService.buscarPorNome(nome));
    }

}
