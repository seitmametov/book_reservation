package com.example.library.mapper;

import com.example.library.Dto.BookCreateRequest;
import com.example.library.Dto.BookResponse;
import com.example.library.enam.BookStatus;
import com.example.library.entity.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getCategory(),
                book.getLocation(),
                book.getStatus()
        );
    }

    public Book toEntity(BookCreateRequest request) {
        return Book.builder()
                .title(request.title())
                .author(request.author())
                .category(request.category())
                .location(request.location())
                .status(BookStatus.AVAILABLE)
                .build();
    }
}
