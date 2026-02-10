package com.example.library.controller;

import com.example.library.Dto.response.UserResponse;
import com.example.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "Получить список пользователей с фильтрацией по статусу одобрения")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers(
            @RequestParam(required = false) Boolean enabled // Параметр необязательный
    ) {
        return userService.getAllUsers(enabled);
    }
}