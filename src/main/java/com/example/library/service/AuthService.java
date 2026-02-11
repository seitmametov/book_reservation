package com.example.library.service;

import com.example.library.Dto.request.ResetPasswordRequest;
import com.example.library.Dto.response.AuthResponse;
import com.example.library.Dto.request.LoginRequest;
import com.example.library.Dto.request.RegisterRequest;
import com.example.library.enam.Role;
import com.example.library.entity.ConfirmationToken;
import com.example.library.entity.User;
import com.example.library.repository.ConfirmationTokenRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.impl.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ConfirmationTokenRepository tokenRepository;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Электронная почта уже существует    ");
        }

        User user = User.builder()
                .firstName(request.firstName()).lastName(request.lastName())
                .email(request.email())
                .password(encoder.encode(request.password()))
                .role(Role.USER)
                .enabled(false)       // Админ пока не видит
                .emailVerified(false)  // Почта не подтверждена
                .build();

        userRepository.save(user);

        ConfirmationToken token = new ConfirmationToken(user, 15);
        tokenRepository.save(token);
        String link = "http://localhost:8080/auth/confirm?token=" + token.getToken();
        emailService.send(user.getEmail(), "Click here to confirm: " + link);

        return new AuthResponse("Пожалуйста, подтвердите свой адрес электронной почты. Проверьте свою почту..");
    }


    public AuthResponse login(LoginRequest request) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserDetailsImpl userDetails =
                (UserDetailsImpl) auth.getPrincipal();

        User user = userDetails.getUser(); // ✅ ВОТ ТАК

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Пожалуйста, сначала подтвердите свой адрес электронной почты.");
        }
        if (!user.isEnabled()) {
            throw new RuntimeException("Учетная запись ожидает подтверждения администратора.");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }


    public String registerAdmin(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Админ уже существует");
        }

        User admin = User.builder()
                .firstName(request.firstName()) // Добавили
                .lastName(request.lastName())
                .email(request.email())
                .password(encoder.encode(request.password()))
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        userRepository.save(admin);

        return "Админ успешно зарегистрировался";
    }
    @Transactional
    public String confirmToken(String token) {
        // В методах подтверждения добавь проверку:
        ConfirmationToken confirmationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Токен не найден"));

        if (confirmationToken.isExpired()) {
            tokenRepository.delete(confirmationToken); // Удаляем мусор
            throw new RuntimeException("Срок действия токена истек! Пожалуйста, запросите новый токен");
        }
        User user = confirmationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        // Теперь токен можно удалить
        tokenRepository.delete(confirmationToken);

        return "Адрес электронной почты подтвержден! Теперь дождитесь подтверждения администратора! ";
    }
    @Transactional
    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Генерим токен (используем наш старый добрый ConfirmationToken)
        ConfirmationToken token = new ConfirmationToken(user, 15);
        tokenRepository.save(token);

        // Ссылка будет вести на фронтенд или на наш эндпоинт
        String link = "http://localhost:8080/auth/reset-password?token=" + token.getToken();
        emailService.send(user.getEmail(), "To reset your password, click: " + link);

        return "Ссылка для сброса пароля будет отправлена на вашу электронную почту.";
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        ConfirmationToken confirmationToken = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new RuntimeException("Недействительный или просроченный токен"));

        User user = confirmationToken.getUser();

        // Хэшируем новый пароль
        user.setPassword(encoder.encode(request.newPassword()));
        userRepository.save(user);

        // Удаляем токен, чтобы его нельзя было юзать второй раз
        tokenRepository.delete(confirmationToken);

        return "Пароль успешно обновлен!";
    }
    // Добавь это в AuthService.java

    @Transactional
    public String initiateEmailChange(User user, String newEmail) {
        if (userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("Этот email уже занят другим пользователем");
        }

        // Генерируем токен.
        // Чтобы знать, какой email подтверждаем, можно временно сохранить его где-то,
        // но проще всего передать его в саму ссылку (параметром)
        ConfirmationToken token = new ConfirmationToken(user, 30); // Даем 30 минут
        tokenRepository.save(token);

        String link = "http://localhost:8080/auth/confirm-email-change?token=" + token.getToken() + "&newEmail=" + newEmail;

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

        // Проверяем еще раз на уникальность перед финальным сохранением
        if (userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("Этот email уже успели занять!");
        }

        user.setEmail(newEmail);
        userRepository.save(user);

        tokenRepository.delete(confirmationToken);

        return "Email успешно обновлен на " + newEmail;
    }
}


