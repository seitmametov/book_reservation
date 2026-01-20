package com.example.library.Dto;

public record BookCreateRequest(
        String title,
        String author,
        String category,
        String location
) {}
