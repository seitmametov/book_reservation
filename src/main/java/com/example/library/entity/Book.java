    package com.example.library.entity;

    import com.example.library.enam.BookStatus;
    import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;
    import org.hibernate.annotations.SQLDelete;
    import org.hibernate.annotations.SQLRestriction;
    import org.hibernate.annotations.SQLRestriction;
    import org.hibernate.annotations.SQLDelete;


    import java.time.LocalDateTime;

    @Entity
    @Table(name = "books")
    @Getter
    @Setter
    @NoArgsConstructor
    @SQLDelete(sql = "UPDATE books SET active = false WHERE id = ?")
    @SQLRestriction("active = true") // Вот этот парень отвечает за GET запросы
    public class Book {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false)
        private String title;

        @Column(nullable = false)
        private String author;

        @Column(length = 1000)
        private String description;

        @ManyToOne
        @JoinColumn(name = "category_id", nullable = false)
        private Category category;

        @Column(nullable = false)
        private String location;

        private String coverUrl;

        @Enumerated(EnumType.STRING)
        private BookStatus status;

        @Column(nullable = false)
        private boolean deleted = false; // Поле для мягкого удаления

        @Column(name = "active")
        private boolean active = true;

        private LocalDateTime createdAt;

        @PrePersist
        public void onCreate() {
            this.createdAt = LocalDateTime.now();
        }

        @Column(nullable = false)
        private Double averageRating = 0.0;

        @Column(nullable = false)
        private Integer reviewCount = 0;

    }