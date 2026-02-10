package com.example.library.Dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @Email(message = "Invalid email")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @NotBlank(message = "First name is required")
        String firstName, // Добавляем имя

        @NotBlank(message = "Last name is required")
        String lastName   // Добавляем фамилию
) {}