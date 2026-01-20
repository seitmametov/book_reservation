package com.example.library.Dto;

import com.example.library.enam.BookStatus;

public record BookResponse(
        Long id,
        String title,
        String author,
        String category,
        String location,
        BookStatus status
) {}
