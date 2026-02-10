package com.example.library.Dto.request;

import jakarta.validation.constraints.Email;

public record ForgotPasswordRequest(@Email String email) {}