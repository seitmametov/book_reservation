package com.example.library.service;

import com.example.library.Dto.BookCreateRequest;
import com.example.library.Dto.BookResponse;
import com.example.library.entity.Book;
import com.example.library.mapper.BookMapper;
import com.example.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        return bookMapper.toResponse(book);
    }

    public BookResponse create(BookCreateRequest request) {
        Book book = bookMapper.toEntity(request);
        return bookMapper.toResponse(bookRepository.save(book));
    }

    public void delete(Long id) {
        bookRepository.deleteById(id);
    }
}
