package com.example.library.controller;

import com.example.library.Dto.request.UpdateProfileRequest;
import com.example.library.Dto.response.UserResponse;
import com.example.library.entity.User;
import com.example.library.service.AuthService;
import com.example.library.service.FileStorageService;
import com.example.library.service.UserService;
import com.example.library.service.impl.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Profile Controller", description = "Управление профилем пользователя и загрузка аватарок")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final AuthService authService;
    // Сюда потом заинжектим MinioService

    @Operation(summary = "Получить данные моего профиля")
    @GetMapping("/me")
    public UserResponse getMe(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.isEnabled(),
                user.getAvatarUrl() // <-- ДОБАВЬ ЭТУ СТРОЧКУ!
        );
    }

    @Operation(summary = "Обновить Имя и Фамилию")
    @PutMapping("/update")
    public UserResponse update(@AuthenticationPrincipal UserDetailsImpl userDetails,
                               @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(userDetails.getUser(), request);
    }

    @Operation(summary = "Загрузить аватарку", description = "Загружает файл в Minio и сохраняет ссылку в профиль")
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadAvatar(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestPart("file") MultipartFile file
    ) {
        // Загружаем в специальный бакет "avatars"
        String avatarUrl = fileStorageService.upload(file, "avatars");

        // Сохраняем ссылку в БД для текущего юзера
        userService.updateAvatar(userDetails.getUser(), avatarUrl);

        return avatarUrl;
    }
    @Operation(summary = "Запросить смену Email", description = "Отправляет ссылку подтверждения на новый адрес")
    @PostMapping("/update-email")
    public ResponseEntity<String> updateEmail(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String newEmail) {
        return ResponseEntity.ok(authService.initiateEmailChange(userDetails.getUser(), newEmail));
    }
}