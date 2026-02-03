package com.example.library.controller;

import com.example.library.Dto.BookCreateRequest;
import com.example.library.Dto.BookResponse;
import com.example.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Tag(name = "Кантроллер Book связан со всеми махинациями которые мы можем тварить с книгами, такие как выдача созлание и тд")
public class BookController {

    private final BookService bookService;

    // user + admin
    @GetMapping
    public List<BookResponse> getAll() {
        return bookService.getAllBooks();
    }

    @GetMapping("/{id}")
    public BookResponse getById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    // admin
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

    @Operation(summary = "Прикрепление обложки для книги, в поле 'id' водим id книги и к нему прикрепиться облодка которая вы вставили")
    @PostMapping("/{id}/cover")
    public BookResponse uploadCover(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return bookService.uploadCover(id, file);
    }



}
