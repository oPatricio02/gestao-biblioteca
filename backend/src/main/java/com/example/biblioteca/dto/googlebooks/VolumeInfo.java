package com.example.biblioteca.dto.googlebooks;

import java.util.List;

public record VolumeInfo(
    String title,
    List<String> authors,
    String publishedDate,
    List<IndustryIdentifier> industryIdentifiers,
    List<String> categories,
    ImageLinks imageLinks
) {}
