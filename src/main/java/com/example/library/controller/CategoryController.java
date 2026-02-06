package com.example.library.controller;

import com.example.library.Dto.BookFilterRequest;
import com.example.library.Dto.BookResponse;
import com.example.library.Dto.CategoryRequest;
import com.example.library.entity.Category;
import com.example.library.service.BookService;
import com.example.library.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Category Controller", description = "Контроллер для управления категориями книг и поиска книг по категориям")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;
    private final BookService bookService;

    @Operation(
            summary = "Создать новую категорию",
            description = "Создание новой категории для книг. Требуется роль ADMIN"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Категория успешно создана",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Category.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные запроса"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав (требуется роль ADMIN)"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Категория с таким названием уже существует"
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Category create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания категории",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CategoryRequest.class)
                    )
            )
            @RequestBody CategoryRequest request
    ) {
        return categoryService.create(
                request.name(),
                request.description()
        );
    }

    @Operation(
            summary = "Получить все категории",
            description = "Возвращает список всех доступных категорий книг"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список категорий успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Category.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            )
    })
    @GetMapping
    public List<Category> getAll() {
        return categoryService.getAll();
    }

    @Operation(
            summary = "Поиск книг по фильтрам",
            description = "Поиск книг с использованием различных фильтров: по категориям, автору, названию и т.д."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Результаты поиска успешно получены",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = BookResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные параметры фильтра"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            )
    })
    @PostMapping("/books/search")
    public List<BookResponse> search(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Параметры фильтрации для поиска книг",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookFilterRequest.class)
                    )
            )
            @RequestBody BookFilterRequest filter
    ) {
        return bookService.search(filter);
    }
}