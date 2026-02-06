package com.example.library.controller;

import com.example.library.Dto.ReservationResponse;
import com.example.library.details.CustomUserDetails;
import com.example.library.entity.User;
import com.example.library.repository.UserRepository;
import com.example.library.service.ReservationService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation Controller", description = "Контроллер для управления бронированиями книг: резервирование, выдача, возврат и просмотр")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Забронировать книгу",
            description = "Создание бронирования для указанной книги. Книга должна быть доступна (status = AVAILABLE)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Книга успешно забронирована",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReservationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Книга недоступна для бронирования"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга не найдена"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "У пользователя уже есть активная бронь этой книги"
            )
    })
    @PostMapping("/{bookId}")
    public ReservationResponse reserve(
            @Parameter(
                    description = "Идентификатор книги для бронирования",
                    required = true,
                    example = "1"
            )
            @PathVariable Long bookId,

            @Parameter(
                    description = "Текущий авторизованный пользователь",
                    hidden = true
            )
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return reservationService.reserveBook(bookId, userDetails.getUser());
    }

    @Operation(
            summary = "Взять забронированную книгу",
            description = "Отметка о фактическом получении ранее забронированной книги пользователем"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Книга успешно выдана"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невозможно выдать книгу (не истек срок брони, книга уже выдана и т.д.)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Бронирование не найдено"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Пользователь не является владельцем бронирования"
            )
    })
    @PostMapping("/{id}/take")
    public void take(
            @Parameter(
                    description = "Идентификатор бронирования",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,

            @Parameter(
                    description = "Текущий авторизованный пользователь",
                    hidden = true
            )
            @AuthenticationPrincipal User user
    ) {
        reservationService.takeBook(id, user);
    }

    @Operation(
            summary = "Вернуть книгу",
            description = "Возврат книги в библиотеку после использования"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Книга успешно возвращена"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невозможно вернуть книгу (книга не была взята и т.д.)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Бронирование не найдено"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Пользователь не является владельцем бронирования"
            )
    })
    @PostMapping("/{id}/return")
    public void returnBook(
            @Parameter(
                    description = "Идентификатор бронирования",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,

            @Parameter(
                    description = "Текущий авторизованный пользователь",
                    hidden = true
            )
            @AuthenticationPrincipal User user
    ) {
        reservationService.returnBook(id, user);
    }

    @Operation(
            summary = "Получить все мои бронирования",
            description = "Возвращает список всех бронирований текущего пользователя (все статусы)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список бронирований успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ReservationResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            )
    })
    @GetMapping("/my")
    public List<ReservationResponse> my(
            @Parameter(
                    description = "Текущий авторизованный пользователь",
                    hidden = true
            )
            @AuthenticationPrincipal User user
    ) {
        return reservationService.myReservations(user);
    }

    @Operation(
            summary = "Получить активные бронирования",
            description = "Возвращает список активных бронирований текущего пользователя (забронированные, но еще не возвращенные книги)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список активных бронирований успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ReservationResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            )
    })
    @GetMapping("/my/active")
    public List<ReservationResponse> myActive(
            @Parameter(
                    description = "Текущий авторизованный пользователь",
                    hidden = true
            )
            @AuthenticationPrincipal User user
    ) {
        return reservationService.myActiveReservations(user);
    }

    @Operation(
            summary = "Отменить бронирование",
            description = "Отмена активного бронирования книги. Можно отменить только свои бронирования"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Бронирование успешно отменено"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невозможно отменить бронирование (уже взята, возвращена и т.д.)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Бронирование не найдено"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Пользователь не является владельцем бронирования"
            )
    })
    @DeleteMapping("/{id}")
    public void cancel(
            @Parameter(
                    description = "Идентификатор бронирования для отмены",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,

            @Parameter(
                    description = "Текущий авторизованный пользователь",
                    hidden = true
            )
            @AuthenticationPrincipal User user
    ) {
        reservationService.cancel(id, user);
    }
}