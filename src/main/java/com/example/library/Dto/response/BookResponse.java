package com.example.library.Dto.response;

import com.example.library.enam.BookStatus;

public record BookResponse(
        Long id,
        String title,
        String author,
        String description,
        String category,
        String location,
        String coverUrl,
        BookStatus status,
        Double averageRating, // Добавлено
        Integer reviewCount   // Добавлено
) {}
