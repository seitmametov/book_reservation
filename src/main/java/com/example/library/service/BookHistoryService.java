package com.example.library.service;

import com.example.library.Dto.response.BookHistoryResponse;
import com.example.library.enam.BookEventType;
import com.example.library.entity.Book;
import com.example.library.entity.BookHistory;
import com.example.library.entity.User;
import com.example.library.mapper.BookHistoryMapper;
import com.example.library.repository.BookHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookHistoryService {

    private final BookHistoryRepository repository;
    private final BookHistoryMapper mapper;

    public BookHistoryService(BookHistoryRepository repository,
                              BookHistoryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public void log(Book book, User user, BookEventType eventType) {
        BookHistory history = new BookHistory();
        history.setBook(book);
        history.setUser(user);
        history.setEventType(eventType);
        history.setCreatedAt(LocalDateTime.now());

        repository.save(history);
    }
    public List<BookHistoryResponse> getHistoryByBookId(Long bookId) {
        return repository.findByBookIdOrderByCreatedAtDesc(bookId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public void log(Book book, BookEventType eventType) {
        log(book, null, eventType);
    }
}
