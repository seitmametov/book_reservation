package com.example.library.Dto;

public record BookRequest(
        String title,
        String author,
        String description,
        Long categoryId,
        String location
) {}

