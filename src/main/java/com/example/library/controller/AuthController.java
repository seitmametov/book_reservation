package com.example.library.controller;

import com.example.library.Dto.request.ForgotPasswordRequest;
import com.example.library.Dto.request.ResetPasswordRequest;
import com.example.library.Dto.response.AuthResponse;
import com.example.library.Dto.request.LoginRequest;
import com.example.library.Dto.request.RegisterRequest;
import com.example.library.entity.User;
import com.example.library.repository.UserRepository;
import com.example.library.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Controller", description = "Регистрация, вход и восстановление доступа")
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;

    @Operation(summary = "Регистрация нового пользователя", description = "Создает аккаунт. После регистрации необходимо подтвердить email.")
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "Вход в систему", description = "Возвращает JWT токен при успешной авторизации.")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Одобрение регистрации (Admin Only)", description = "Активация аккаунта пользователя администратором. Только после подтверждения почты.")
    @PatchMapping("/admin/users/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public void approve(@Parameter(description = "ID пользователя для одобрения") @PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Невозможно одобрить пользователя: адрес электронной почты еще не подтвержден!");
        }

        user.setEnabled(true);
        userRepository.save(user);
    }

    @Operation(summary = "Регистрация администратора", description = "Создание пользователя с правами ADMIN.")
    @PostMapping("/register-admin")
    public ResponseEntity<String> registerAdmin(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerAdmin(request));
    }

    @Operation(summary = "Подтверждение Email", description = "Активация токена, пришедшего на почту после регистрации.")
    @GetMapping("/confirm")
    public ResponseEntity<String> confirm(@RequestParam("token") String token) {
        return ResponseEntity.ok(authService.confirmToken(token));
    }

    @Operation(summary = "Запрос на восстановление пароля", description = "Отправляет ссылку со сбросом пароля на указанный email.")
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request.email()));
    }

    @Operation(summary = "Установка нового пароля", description = "Принимает токен из письма и новый пароль.")
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @Operation(summary = "Тестовый эндпоинт", hidden = true)
    @GetMapping("/test")
    public String test() {
        return "test";
    }
    @Operation(summary = "Подтверждение смены Email", description = "Эндпоинт, по которому кликает юзер в письме")
    @GetMapping("/confirm-email-change")
    public ResponseEntity<String> confirmEmailChange(
            @RequestParam("token") String token,
            @RequestParam("newEmail") String newEmail) {
        return ResponseEntity.ok(authService.confirmEmailChange(token, newEmail));
    }
}