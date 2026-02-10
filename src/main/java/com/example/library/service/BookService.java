package com.example.library.service;

import com.example.library.Dto.request.BookCreateRequest;
import com.example.library.Dto.request.BookFilterRequest;
import com.example.library.Dto.response.BookResponse;
import com.example.library.Specification.BookSpecification;
import com.example.library.enam.SortDirection;
import com.example.library.entity.Book;
import com.example.library.mapper.BookMapper;
import com.example.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final FileStorageService fileStorageService;



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

    // Этот метод теперь пометит книгу как deleted = true вместо физического удаления
    // СТАРЫЙ МЕТОД (Удаляет из БД совсем)
    public void delete(Long id) {
        bookRepository.deleteById(id);
    }
    public void softDelete(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        book.setDeleted(true);
        bookRepository.save(book);
    }
    // Дополнительный метод для восстановления книги
    public void restore(Long id) {
        Book book = bookRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        book.setDeleted(false);
        bookRepository.save(book);
    }

    public BookResponse uploadCover(Long bookId, MultipartFile file) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        String url = fileStorageService.upload(file);
        book.setCoverUrl(url);

        return bookMapper.toResponse(bookRepository.save(book));
    }

    //28.01.26


    public List<BookResponse> search(BookFilterRequest filter) {

        Specification<Book> spec = Specification
                .where(BookSpecification.search(filter.getSearch()))
                .and(BookSpecification.category(filter.getCategoryId()))
                .and(BookSpecification.status(filter.getStatus()))
                .and(BookSpecification.author(filter.getAuthor()))
                .and(BookSpecification.location(filter.getLocation()))
                .and(BookSpecification.minRating(filter.getMinRating()))
                .and(BookSpecification.tags(filter.getTags()));

        return bookRepository.findAll(spec, buildSort(filter))
                .stream()
                .map(bookMapper::toResponse)
                .toList();
    }
    private Sort buildSort(BookFilterRequest filter) {
        if (filter.getSortBy() == null) {
            return Sort.unsorted();
        }

        Sort.Direction direction =
                filter.getSortDirection() == SortDirection.DESC
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        return switch (filter.getSortBy()) {
            case TITLE -> Sort.by(direction, "title");
            case AUTHOR -> Sort.by(direction, "author");
            case CREATED_AT -> Sort.by(direction, "createdAt");
            case RATING -> Sort.by(direction, "rating");
            case POPULARITY -> Sort.by(direction, "popularity");
        };
    }



}
