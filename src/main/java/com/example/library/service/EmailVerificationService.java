package com.example.library.service;

import com.example.library.entity.EmailVerificationToken;
import com.example.library.entity.User;
import com.example.library.repository.EmailVerificationTokenRepository;
import com.example.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepo;
    private final UserRepository userRepo;

    public void verify(String token) {

        EmailVerificationToken t = tokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (t.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = t.getUser();
        user.setEmailVerified(true);

        userRepo.save(user);
        tokenRepo.delete(t);
    }
}
