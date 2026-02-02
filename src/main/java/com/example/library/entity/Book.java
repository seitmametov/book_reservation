package com.example.library.entity;

import com.example.library.enam.BookStatus;
import jakarta.persistence.*;
import lombok.*;

//@Entity
//@Table(name = "books")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Book {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String title;
//
//    @Column(nullable = false)
//    private String author;
//
//    private String category;
//
//    @Column(nullable = false)
//    private String location;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private BookStatus status;
//
//}

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
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

    // где лежит книга (полка, шкаф и тд)
    @Column(nullable = false)
    private String location;

    // ссылка на обложку (пока String, потом MinIO)
    private String coverUrl;

    @Enumerated(EnumType.STRING)
    private BookStatus status;

}

