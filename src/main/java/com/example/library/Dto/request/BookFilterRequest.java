package com.example.library.Dto.request;

import com.example.library.enam.BookStatus;
import com.example.library.enam.SortDirection;
import com.example.library.enam.SortField;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookFilterRequest {

    // 🔎 Поиск
    private String search; // title + author

    // 🎛 Фильтры
    private Long categoryId;
    private BookStatus status; // AVAILABLE / RESERVED / TAKEN
    private String author;
    private String location;

    private List<String> tags;

    private Double minRating;

    // 🔃 Сортировка
    private SortField sortBy;
    private SortDirection sortDirection;

    //------------------ дата добавления книги
    private LocalDate dateFrom;
    private LocalDate dateTo;
    //-------------------

}
