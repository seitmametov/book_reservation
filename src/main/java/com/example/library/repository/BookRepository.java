package com.example.library.repository;

import com.example.library.enam.BookStatus;
import com.example.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    List<Book> findByStatus(BookStatus status);

    // Метод для нахождения книги, даже если она помечена как удаленная
    @Query(value = "SELECT * FROM books WHERE id = :id", nativeQuery = true)
    Optional<Book> findByIdIncludingDeleted(@Param("id") Long id);

    boolean existsByCategoryId(Long categoryId);

    @Query(value = "SELECT count(*) > 0 FROM books WHERE category_id = :categoryId", nativeQuery = true)
    boolean existsAnyByCategoryId(@Param("categoryId") Long categoryId);

    // Поиск всех книг категории (включая удаленные)
    List<Book> findAllByCategoryId(Long categoryId);

    // Достаем АБСОЛЮТНО все книги этой категории, игнорируя фильтры Hibernate
    @Query(value = "SELECT * FROM books WHERE category_id = :categoryId", nativeQuery = true)
    List<Book> findAllByCategoryIdIncludingHidden(@Param("categoryId") Long categoryId);

    @Query("SELECT b FROM Book b WHERE b.active = tr    ue")
    List<Book> findAllActive();

    @Modifying
    @Transactional
    @Query("UPDATE Book b SET b.active = false WHERE b.id = :id")
    void setBookInactive(@Param("id") Long id);

    // Для метода restore (чтобы найти даже удаленную)
}
