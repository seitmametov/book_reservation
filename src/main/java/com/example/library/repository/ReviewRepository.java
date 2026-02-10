package com.example.library.repository;

import com.example.library.entity.Book;
import com.example.library.entity.Review;
import com.example.library.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Тот самый метод, который не мог найти компилятор
    Optional<Review> findByBookAndUser(Book book, User user);

    // Метод для получения списка отзывов
    List<Review> findByBookIdOrderByCreatedAtDesc(Long bookId);

    // Метод для подсчета количества отзывов
    Long countByBookId(Long bookId);

    // Метод для вычисления среднего значения рейтинга
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Double getAverageRatingByBookId(@Param("bookId") Long bookId);
}