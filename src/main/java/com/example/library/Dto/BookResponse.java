package com.example.library.Dto;

import com.example.library.enam.BookStatus;
import com.example.library.entity.Book;

public record BookResponse(
        Long id,
        String title,
        String author,
        String description,
        String category,
        String location,
        String coverUrl,
        BookStatus status
){
}
