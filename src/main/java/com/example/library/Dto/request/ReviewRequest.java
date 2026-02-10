package com.example.library.Dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

// Запрос на создание отзыва
public record ReviewRequest(
        @Schema(example = "5", description = "Оценка от 1 до 5")
        Integer rating,
        @Schema(example = "Очень интересная книга!", description = "Текст отзыва")
        String comment
) {}

