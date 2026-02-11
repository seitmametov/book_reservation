package com.example.library.service;

import com.example.library.Dto.request.UpdateProfileRequest;
import com.example.library.Dto.response.UserResponse;
import com.example.library.entity.User;
import com.example.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> getAllUsers(Boolean enabled) {
        return userRepository.findAll().stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRole().name(),
                        user.isEnabled(),
                        user.getAvatarUrl()
                ))
                .toList();
    }
    @Transactional
    public UserResponse updateProfile(User user, UpdateProfileRequest request) {
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getRole().name(),
                savedUser.isEnabled(),
                savedUser.getAvatarUrl()
        );
    }

    // Метод для сохранения ссылки на аватарку (вызовем позже)
    @Transactional
    public void updateAvatar(User user, String avatarUrl) {
        user.setAvatarUrl(avatarUrl); // Добавь это поле в сущность User!
        userRepository.save(user);
    }
    public UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.isEnabled(),
                user.getAvatarUrl() // Передаем ссылку в DTO
        );
    }
}