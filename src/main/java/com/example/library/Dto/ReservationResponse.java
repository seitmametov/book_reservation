package com.example.library.Dto;

import com.example.library.enam.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long bookId,
        String bookTitle,
        LocalDateTime reservedAt,
        LocalDateTime takenAt,    // Добавили
        LocalDateTime returnedAt,  // Добавили
        ReservationStatus status

) {}
