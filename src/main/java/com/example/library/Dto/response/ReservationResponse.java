package com.example.library.Dto.response;

import com.example.library.enam.ReservationStatus;
import java.time.LocalDateTime;

/**
 * DTO для ответа по бронированию.
 * Теперь включает полную информацию о пользователе для админ-панели.
 */
public record ReservationResponse(
        Long id,
        UserResponse user,         // Теперь объект UserResponse стоит здесь
        Long bookId,
        String bookTitle,
        LocalDateTime reservedAt,
        LocalDateTime takenAt,
        LocalDateTime returnedAt,
        ReservationStatus status
) {}