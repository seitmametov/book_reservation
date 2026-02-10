package com.example.library.Dto.request;

public record BookCreateRequest(
        String title,
        String author,
        String description,
        Long categoryId,
        String location
) {}
