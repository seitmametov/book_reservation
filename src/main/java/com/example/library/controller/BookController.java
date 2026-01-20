package com.example.library.controller;

import com.example.library.Dto.BookCreateRequest;
import com.example.library.Dto.BookResponse;
import com.example.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // USER + ADMIN
    @GetMapping
    public List<BookResponse> getAll() {
        return bookService.getAllBooks();
    }

    @GetMapping("/{id}")
    public BookResponse getById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    // ADMIN
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse create(@RequestBody BookCreateRequest request) {
        return bookService.create(request);
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        bookService.delete(id);
    }
}
