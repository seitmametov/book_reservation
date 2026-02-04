package com.example.library.service;

import com.example.library.Dto.AuthResponse;
import com.example.library.Dto.LoginRequest;
import com.example.library.Dto.RegisterRequest;
import com.example.library.enam.Role;
import com.example.library.entity.EmailVerificationToken;
import com.example.library.entity.User;
import com.example.library.exceptions.EmailAlreadyExistsException;
import com.example.library.repository.EmailVerificationTokenRepository;
import com.example.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailVerificationTokenRepository tokenRepository;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .email(request.email())
                .password(encoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(Role.USER)
                .emailVerified(false)
                .enabled(false)
                .build();

        userRepository.save(user);

        String token = UUID.randomUUID().toString();

        EmailVerificationToken verificationToken =
                EmailVerificationToken.builder()
                        .token(token)
                        .user(user)
                        .expiresAt(LocalDateTime.now().plusHours(24))
                        .build();

        tokenRepository.save(verificationToken);

// Тут позже будет emailService.send(...)
        System.out.println("VERIFY LINK: http://localhost:8080/auth/verify?token=" + token);

        return new AuthResponse(
                "Registration successful. Please verify your email."
        );
    }




    public AuthResponse login(LoginRequest request) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = (User) auth.getPrincipal();

        String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }

}


