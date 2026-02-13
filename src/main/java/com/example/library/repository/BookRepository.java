package com.example.library.repository;

import com.example.library.enam.BookStatus;
import com.example.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    List<Book> findByStatus(BookStatus status);

    // Метод для нахождения книги, даже если она помечена как удаленная
    @Query(value = "SELECT * FROM books WHERE id = :id", nativeQuery = true)
    Optional<Book> findByIdIncludingDeleted(@Param("id") Long id);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByCategoryIdAndDeletedFalse(Long categoryId);

    // Поиск всех книг категории (включая удаленные)
    List<Book> findAllByCategoryId(Long categoryId);
}
