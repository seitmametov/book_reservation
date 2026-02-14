package com.example.library.service;

import com.example.library.Dto.request.ReviewRequest;
import com.example.library.Dto.response.ReviewResponse;
import com.example.library.entity.Book;
import com.example.library.entity.Review;
import com.example.library.entity.User;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ReviewRepository;
import com.example.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse addReview(Long bookId, ReviewRequest request, String email) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверяем, не оставлял ли юзер отзыв ранее
        Review review = reviewRepository.findByBookAndUser(book, user)
                .orElse(new Review());

        boolean isNew = review.getId() == null;

        review.setBook(book);
        review.setUser(user);
        review.setRating(request.rating());
        review.setComment(request.comment());

        reviewRepository.save(review);

        // Пересчитываем рейтинг книги
        updateBookRating(book);

        return new ReviewResponse(
                book.getId(),
                user.getEmail(),
                review.getRating(),
                review.getComment(),
                java.time.LocalDateTime.now() // Или просто LocalDateTime.now() если есть импорт
        );
    }
    // Этот метод должен быть ВНУТРИ класса ReviewService
    private void updateBookRating(Book book) {
        // Достаем среднее значение из репозитория отзывов
        Double avg = reviewRepository.getAverageRatingByBookId(book.getId());
        // Достаем количество отзывов
        Long count = reviewRepository.countByBookId(book.getId());

        // Обновляем поля в сущности книги
        // Если отзывов нет (avg == null), ставим 0.0
        book.setAverageRating(avg != null ? avg : 0.0);
        book.setReviewCount(count.intValue());

        // Сохраняем обновленную книгу в БД
        bookRepository.save(book);
    }

    public List<ReviewResponse> getBookReviews(Long bookId) {
        return reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId).stream()
                .map(r -> new ReviewResponse(r.getId(), r.getUser().getEmail(), r.getRating(), r.getComment(), r.getCreatedAt()))
                .toList();
    }

    public Double getAverageRating(Long bookId) {
        return reviewRepository.getAverageRatingByBookId(bookId);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request, String email) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));

        // ПРОВЕРКА: Только автор может редактировать
        if (!review.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Вы можете редактировать только свой отзыв!");
        }

        review.setRating(request.rating());
        review.setComment(request.comment());
        reviewRepository.save(review);

        // Обновляем рейтинг книги
        updateBookRating(review.getBook());

        return new ReviewResponse(
                review.getId(),
                review.getUser().getEmail(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
    @Transactional
    public void deleteReview(Long reviewId, String email, boolean isAdmin) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));

        // ПРОВЕРКА: Удалить может либо автор, либо админ
        boolean isOwner = review.getUser().getEmail().equals(email);

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("У вас нет прав на удаление этого отзыва!");
        }

        Book book = review.getBook();
        reviewRepository.delete(review);

        // Пересчитываем рейтинг книги после удаления
        updateBookRating(book);
    }
    
}
