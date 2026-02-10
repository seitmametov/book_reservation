package com.example.library.mapper;

import com.example.library.Dto.request.BookCreateRequest;
import com.example.library.Dto.response.BookResponse;
import com.example.library.enam.BookStatus;
import com.example.library.entity.Book;
import com.example.library.entity.Category;
import com.example.library.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookMapper {

    private final CategoryRepository categoryRepository;

    public Book toEntity(BookCreateRequest request) {

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Book book = new Book();
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setDescription(request.description());
        book.setLocation(request.location());
        book.setCategory(category);
        book.setStatus(BookStatus.AVAILABLE);

        return book;
    }

    public BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getDescription(),
                book.getCategory().getName(),
                book.getLocation(),
                book.getCoverUrl(),
                book.getStatus(),
                book.getAverageRating(),
                book.getReviewCount()
        );
    }
}

