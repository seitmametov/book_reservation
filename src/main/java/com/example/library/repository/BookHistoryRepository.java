package com.example.library.repository;

import com.example.library.entity.BookHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookHistoryRepository
        extends JpaRepository<BookHistory, Long> {

    List<BookHistory> findByBookIdOrderByCreatedAtDesc(Long bookId);
}

