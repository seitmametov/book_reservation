package com.example.library.service;

import com.example.library.Dto.request.ResetPasswordRequest;
import com.example.library.Dto.response.AuthResponse;
import com.example.library.Dto.request.LoginRequest;
import com.example.library.Dto.request.RegisterRequest;
import com.example.library.enam.BookStatus;
import com.example.library.enam.ReservationStatus;
import com.example.library.enam.Role;
import com.example.library.entity.ConfirmationToken;
import com.example.library.entity.Reservation;
import com.example.library.entity.User;
import com.example.library.repository.ConfirmationTokenRepository;
import com.example.library.repository.ReservationRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.impl.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ConfirmationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final ReservationRepository reservationRepository;

    @org.springframework.beans.factory.annotation.Value("${app.url}")
    private String appUrl;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Ищем пользователя (включая удаленных/архивных)
        Optional<User> existingUser = userRepository.findByEmailIncludingDeleted(request.email());

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (user.isDeleted()) {
                // ВОССТАНОВЛЕНИЕ: Оживляем старую запись
                user.setFirstName(request.firstName());
                user.setLastName(request.lastName());
                user.setPassword(encoder.encode(request.password()));
                user.setDeleted(false);
                user.setEnabled(false);
                user.setEmailVerified(false);
                user = userRepository.save(user);
            } else {
                throw new RuntimeException("Электронная почта уже занята");
            }
        } else {
            // СОЗДАНИЕ: Если юзера никогда не было
            user = User.builder()
                    .firstName(request.firstName())
                    .lastName(request.lastName())
                    .email(request.email())
                    .password(encoder.encode(request.password()))
                    .role(Role.USER)
                    .enabled(false)
                    .emailVerified(false)
                    .deleted(false)
                    .build();
            user = userRepository.save(user);
        }

        // 2. ЧИСТКА ТОКЕНОВ (Критически важно!)
        // Удаляем старые токены этого юзера, если они остались
        tokenRepository.deleteByUserId(user.getId());

        // Принудительно применяем удаление в БД прямо сейчас,
        // чтобы не было ошибки Unique Constraint при вставке нового токена
        tokenRepository.flush();

        // 3. Создаем новый токен подтверждения
        ConfirmationToken token = new ConfirmationToken(user, 15);
        tokenRepository.save(token);

        // 4. Отправка почты
        String link = appUrl + "/auth/confirm?token=" + token.getToken();
        emailService.send(user.getEmail(), "Подтвердите регистрацию: " + link);

        return new AuthResponse("Пожалуйста, подтвердите свой адрес электронной почты. Письмо отправлено.");
    }

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User user = userDetails.getUser();

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Пожалуйста, сначала подтвердите свой адрес электронной почты.");
        }
        if (!user.isEnabled()) {
            throw new RuntimeException("Учетная запись ожидает подтверждения администратора.");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }

    public AuthResponse registerAdmin(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Админ уже существует");
        }

        User admin = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(encoder.encode(request.password()))
                .role(Role.ADMIN)
                .enabled(true)
                .emailVerified(true) // Почта сразу подтверждена
                .build();

        userRepository.save(admin);

        // Сразу генерируем токен, чтобы фронтенд мог его сохранить
        String token = jwtService.generateToken(admin);
        return new AuthResponse(token);
    }

    @Transactional
    public String confirmToken(String token) {
        ConfirmationToken confirmationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Токен не найден"));

        if (confirmationToken.isExpired()) {
            tokenRepository.delete(confirmationToken);
            throw new RuntimeException("Срок действия токена истек! Пожалуйста, запросите новый токен");
        }

        User user = confirmationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        tokenRepository.delete(confirmationToken);

        return "Адрес электронной почты подтвержден! Теперь дождитесь подтверждения администратора! ";
    }

    @Transactional
    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // ЧИСТКА: Если юзер спамит кнопкой "Забыл пароль", удаляем старый токен перед новым INSERT
        tokenRepository.deleteByUserId(user.getId());

        ConfirmationToken token = new ConfirmationToken(user, 15);
        tokenRepository.save(token);

        String link = appUrl + "/auth/reset-password?token=" + token.getToken();
        emailService.send(user.getEmail(), "Чтобы сбросить пароль, нажмите здесь.: " + link);

        return "Ссылка для сброса пароля будет отправлена на вашу электронную почту.";
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        ConfirmationToken confirmationToken = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new RuntimeException("Недействительный или просроченный токен"));

        User user = confirmationToken.getUser();
        user.setPassword(encoder.encode(request.newPassword()));
        userRepository.save(user);

        tokenRepository.delete(confirmationToken);

        return "Пароль успешно обновлен!";
    }

    @Transactional
    public String initiateEmailChange(User user, String newEmail) {
        if (userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("Этот email уже занят другим пользователем");
        }

        // ЧИСТКА: Удаляем старый токен смены почты, если он был
        tokenRepository.deleteByUserId(user.getId());

        ConfirmationToken token = new ConfirmationToken(user, 30);
        tokenRepository.save(token);

        String link = appUrl + "/auth/confirm-email-change?token=" + token.getToken() + "&newEmail=" + newEmail;
        emailService.send(newEmail, "Подтвердите смену email. Кликните по ссылке: " + link);

        return "Ссылка для подтверждения отправлена на вашу новую почту.";
    }

    @Transactional
    public String confirmEmailChange(String token, String newEmail) {
        ConfirmationToken confirmationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Недействительный токен"));

        if (confirmationToken.isExpired()) {
            tokenRepository.delete(confirmationToken);
            throw new RuntimeException("Срок действия токена истек");
        }

        User user = confirmationToken.getUser();

        if (userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("Этот email уже успели занять!");
        }

        user.setEmail(newEmail);
        userRepository.save(user);

        tokenRepository.delete(confirmationToken);

        return "Email успешно обновлен на " + newEmail;
    }
    @Transactional
    public void deleteUserByAdmin(Long userId, String adminPassword, String adminEmail) {
        // 1. Проверка админа
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Администратор не найден"));

        if (!encoder.matches(adminPassword, admin.getPassword())) {
            throw new RuntimeException("Неверный пароль администратора.");
        }

        // 2. Проверка долгов пользователя (ИСПРАВЛЕНО НА ByUser_Id)
        boolean hasBooksAtHand = reservationRepository.existsByUser_IdAndStatus(userId, ReservationStatus.COMPLETED);
        if (hasBooksAtHand) {
            throw new RuntimeException("Нельзя удалить: пользователь еще не вернул книги в библиотеку!");
        }

        // 3. Обработка активных броней (ИСПРАВЛЕНО НА ByUser_Id)
        List<Reservation> activeRes = reservationRepository.findByUser_IdAndStatus(userId, ReservationStatus.ACTIVE);
        for (Reservation res : activeRes) {
            res.setStatus(ReservationStatus.CANCELLED);
            if (res.getBook() != null) {
                res.getBook().setStatus(BookStatus.AVAILABLE);
            }
        }

        // 4. Мягкое удаление
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        userToDelete.setDeleted(true);
        userToDelete.setEnabled(false);
        userRepository.save(userToDelete);
    }

}