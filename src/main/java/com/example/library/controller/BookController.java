package com.example.library.controller;

import com.example.library.Dto.request.BookCreateRequest;
import com.example.library.Dto.request.BookRequest;
import com.example.library.Dto.response.BookHistoryResponse;
import com.example.library.Dto.response.BookResponse;
import com.example.library.Dto.response.ReservationResponse;
import com.example.library.enam.ReservationStatus;
import com.example.library.service.BookHistoryService;
import com.example.library.service.BookService;
import com.example.library.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Tag(name = "Book Controller", description = "Контроллер для управления книгами: создание, получение, удаление книг и управление обложками")
@SecurityRequirement(name = "bearerAuth")
public class BookController {

    private final BookService bookService;
    private final BookHistoryService bookHistoryService;
    private final ReservationService reservationService;


    @Operation(
            summary = "Получить все книги",
            description = "Возвращает список всех книг в библиотеке. Доступно для пользователей с ролью USER и ADMIN"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список книг успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookResponse.class, type = "array")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            )
    })
    @GetMapping
    public List<BookResponse> getAll() {
        return bookService.getAllBooks();
    }

    @Operation(
            summary = "Получить книгу по ID",
            description = "Возвращает информацию о конкретной книге по её идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Книга найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга с указанным ID не найдена"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            )
    })
    @GetMapping("/{id}")
    public BookResponse getById(
            @Parameter(
                    description = "Идентификатор книги",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        return bookService.getBookById(id);
    }

    // ADMIN
    @PostMapping("/admin/create")
    @Operation(
            summary = "Создать новую книгу",
            description = "Создание новой книги в библиотеке. Требуется роль ADMIN"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Книга успешно создана",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookResponse.class)
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
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания книги",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookCreateRequest.class)
                    )
            )
            @RequestBody BookCreateRequest request
    ) {
        return bookService.create(request);
    }

    @Operation(
            summary = "Удалить книгу",
            description = "Удаление книги из библиотеки по её ID. Требуется роль ADMIN"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Книга успешно удалена"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга с указанным ID не найдена"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав (требуется роль ADMIN)"
            )
    })
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(
            @Parameter(
                    description = "Идентификатор книги для удаления",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        bookService.delete(id);
    }

    @Operation(
            summary = "Загрузить обложку для книги",
            description = "Загрузка изображения обложки для указанной книги. Поддерживаемые форматы: JPEG, PNG"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Обложка успешно загружена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный файл или формат изображения"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга с указанным ID не найдена"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            )
    })
    @PostMapping(
            value = "/{id}/cover",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BookResponse uploadCover(
            @Parameter(
                    description = "Идентификатор книги",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,

            @Parameter(
                    description = "Файл обложки (изображение)",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("file") MultipartFile file
    ) {
        return bookService.uploadCover(id, file);
    }
    // НОВЫЙ ЭНДПОИНТ для мягкого удаления
    @Operation(summary = "Мягкое удаление книги (архивация)")
    @PatchMapping("/admin/soft-delete/{id}") // Используем PATCH, так как обновляем поле
    @PreAuthorize("hasRole('ADMIN')")
    public void softDelete(@PathVariable Long id) {
        bookService.softDelete(id);
    }

    @Operation(
            summary = "Получить историю книги",
            description = "Возвращает историю действий с книгой. Доступно только для ADMIN"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "История получена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль ADMIN)")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/history")
    public List<BookHistoryResponse> getBookHistory(@PathVariable Long id) {
        return bookHistoryService.getHistoryByBookId(id);
    }
    // ADMIN: Получить список всех бронирований в системе
    @Operation(
            summary = "Получить список всех бронирований (для админа)",
            description = "Возвращает список всех записей о бронировании с возможностью фильтрации по статусу"
    )
    @GetMapping("/admin/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReservationResponse> getAllReservations(
            @Parameter(description = "Фильтр по статусу (ACTIVE, COMPLETED, RETURNED, CANCELLED)")
            @RequestParam(required = false) ReservationStatus status
    ) {
        return reservationService.getAllReservationsForAdmin(status);
    }
    @Operation(summary = "Редактировать книгу", description = "Обновление данных книги. Нужен ADMIN")
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse update(@PathVariable Long id, @RequestBody BookRequest request) {
        return bookService.update(id, request);
    }

}