package com.example.biblioteca.controller;

import com.example.biblioteca.dto.CriarUsuarioRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping ("/usuarios")
public class UsuarioController {

    @PostMapping
    ResponseEntity<String> create(@Validated @RequestBody CriarUsuarioRequest request){
        return ResponseEntity.accepted().build();
    }


}
