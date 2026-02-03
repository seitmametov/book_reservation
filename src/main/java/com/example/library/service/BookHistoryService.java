package com.example.library.service;

import com.example.library.enam.BookEventType;
import com.example.library.entity.Book;
import com.example.library.entity.BookHistory;
import com.example.library.entity.User;
import com.example.library.repository.BookHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BookHistoryService {

    private final BookHistoryRepository repository;

    public BookHistoryService(BookHistoryRepository repository) {
        this.repository = repository;
    }

    public void log(Book book, User user, BookEventType eventType) {
        BookHistory history = new BookHistory();
        history.setBook(book);
        history.setUser(user);
        history.setEventType(eventType);
        history.setCreatedAt(LocalDateTime.now());

        repository.save(history);
    }

    public void log(Book book, BookEventType eventType) {
        log(book, null, eventType);
    }
}
