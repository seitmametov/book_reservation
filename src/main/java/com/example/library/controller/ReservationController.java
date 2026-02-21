package com.example.library.controller;

import com.example.library.Dto.response.ReservationResponse;
import com.example.library.service.ReservationService;
import com.example.library.service.impl.UserDetailsImpl;
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

    @Operation(summary = "Забронировать книгу", description = "Создание бронирования для указанной книги.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Книга успешно забронирована"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @PostMapping("/{bookId}")
    public ReservationResponse reserve(
            @PathVariable Long bookId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return reservationService.reserveBook(bookId, userDetails.getUser());
    }

    @Operation(summary = "Взять забронированную книгу", description = "Отметка о фактическом получении книги")
    @PostMapping("/{id}/take")
    public void take(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        reservationService.takeBook(id, userDetails.getUser());
    }

    @Operation(summary = "Вернуть книгу", description = "Возврат книги в библиотеку")
    @PostMapping("/{id}/return")
    public void returnBook(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        reservationService.returnBook(id, userDetails.getUser());
    }

    @Operation(summary = "Получить все мои бронирования")
    @GetMapping("/my")
    public List<ReservationResponse> my(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return reservationService.myReservations(userDetails.getUser());
    }

    @Operation(summary = "Получить активные бронирования")
    @GetMapping("/my/active")
    public List<ReservationResponse> myActive(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return reservationService.myActiveReservations(userDetails.getUser());
    }
    @GetMapping("/my/history")
    public List<ReservationResponse> getMyHistory(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return reservationService.getMyReadingHistory(userDetails.getUser());
    }

    @Operation(summary = "Отменить бронирование")
    @DeleteMapping("/{id}")
    public void cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        reservationService.cancel(id, userDetails.getUser());
    }

    @Operation(summary = "Получить список просроченных книг", description = "Доступно только администратору")
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReservationResponse> overdue() {
        return reservationService.getOverdueReservations();
    }
} 