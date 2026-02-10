package com.example.library.Dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        String userEmail,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {}