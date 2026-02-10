package com.example.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"book_id", "user_id"}) // Ограничение: 1 отзыв от юзера на книгу
})
@Getter @Setter @NoArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Book book;

    @ManyToOne(optional = false)
    private User user;

    @Min(1) @Max(5)
    private Integer rating;

    private String comment;
    private LocalDateTime createdAt = LocalDateTime.now();


    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}