package com.example.library.Dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Чтобы отправить новый пароль
public record ResetPasswordRequest(
        @NotBlank String token,
        @Size(min = 6) String newPassword
) {}