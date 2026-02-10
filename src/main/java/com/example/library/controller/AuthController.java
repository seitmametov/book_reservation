package com.example.library.controller;


import com.example.library.Dto.request.ForgotPasswordRequest;
import com.example.library.Dto.request.ResetPasswordRequest;
import com.example.library.Dto.response.AuthResponse;
import com.example.library.Dto.request.LoginRequest;
import com.example.library.Dto.request.RegisterRequest;
import com.example.library.entity.User;
import com.example.library.repository.UserRepository;
import com.example.library.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    private final UserRepository userRepository;

    @GetMapping("/test")
    public String test() {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!test пройден!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return "test";
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PatchMapping("/admin/users/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public void approve(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Невозможно одобрить пользователя: адрес электронной почты еще не подтвержден!");
        }

        user.setEnabled(true);
        userRepository.save(user);
    }
    @PostMapping("/register-admin")
    public ResponseEntity<String> registerAdmin(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.registerAdmin(request));
    }
    @GetMapping("/confirm")
    public ResponseEntity<String> confirm(@RequestParam("token") String token) {
        return ResponseEntity.ok(authService.confirmToken(token));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request.email()));
    }

    // В реальности этот метод должен открывать страницу,
// но для теста в Swagger сделаем POST, принимающий токен и пароль
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }



}
