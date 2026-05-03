package com.example.biblioteca.services;

import com.example.biblioteca.dto.googlebooks.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoogleBooksClient {

    private final RestTemplate restTemplate;
    
    @Value("${api.key.google-book:}")
    private String apiKey;
    
    private static final String GOOGLE_BOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes";

    public List<LivroExternoDto> buscarLivros(String titulo) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(GOOGLE_BOOKS_API_URL)
                .queryParam("q", titulo)
                .queryParam("maxResults", 30);
                
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            builder.queryParam("key", apiKey);
        }
                
        String url = builder.toUriString();

        GoogleBooksResponse response = restTemplate.getForObject(url, GoogleBooksResponse.class);

        if (response == null || response.items() == null) {
            return new ArrayList<>();
        }

        return response.items().stream()
                .map(this::mapToDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private LivroExternoDto mapToDto(GoogleBookItem item) {
        VolumeInfo info = item.volumeInfo();

        String isbn = getInfoIsbn(info);
        if (isbn == null) return null;

        String categoria = (info.categories() != null && !info.categories().isEmpty()) 
                ? info.categories().getFirst() : "Geral";

        String data = getData(info);

        String thumbnailUrl = null;
        if (info.imageLinks() != null) {
            thumbnailUrl = info.imageLinks().thumbnail() != null
                ? info.imageLinks().thumbnail()
                : info.imageLinks().smallThumbnail();
        }

        return new LivroExternoDto(
                item.id(),
                info.title() != null ? info.title() : "Título Desconhecido",
                info.authors() != null ? info.authors() : List.of("Autor Desconhecido"),
                isbn,
                categoria,
                data,
                thumbnailUrl
        );
    }

    private String getData(VolumeInfo info) {
        String data = info.publishedDate();
        if (data == null) {
            return "1800-01-01";
        } else if (data.length() == 4) {
            data = data + "-01-01";
        } else if (data.length() == 7) {
            data = data + "-01";
        }
        return data;
    }

    private String getInfoIsbn(VolumeInfo info) {
        if (info == null) {
            return null;
        }

        String isbn = null;
        if (info.industryIdentifiers() != null) {
            isbn = getIsbn(info);
        }

        return isbn;
    }

    private String getIsbn(VolumeInfo info) {
        String isbn = null;
        for (IndustryIdentifier id : info.industryIdentifiers()) {
            if ("ISBN_13".equals(id.type())) {
                isbn = id.identifier();
            } else if ("ISBN_10".equals(id.type())) {
                isbn = id.identifier();
            }
        }
        return isbn;
    }
}
