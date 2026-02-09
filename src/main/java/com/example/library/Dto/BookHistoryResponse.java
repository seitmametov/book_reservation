package com.example.library.Dto;

import com.example.library.enam.BookEventType;

import java.time.LocalDateTime;

public record BookHistoryResponse(
        Long id,
        BookEventType eventType,
        String userEmail,
        String comment,
        LocalDateTime createdAt
) {}
