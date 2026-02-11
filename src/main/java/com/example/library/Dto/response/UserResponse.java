package com.example.library.Dto.response;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String role,
        boolean enabled,
        String avatarUrl// Чтобы в общем списке было видно статус
) {}