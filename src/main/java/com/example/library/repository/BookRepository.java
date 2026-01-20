package com.example.library.repository;

import com.example.library.enam.BookStatus;
import com.example.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByStatus(BookStatus status);

    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
            String title, String author
    );
}
