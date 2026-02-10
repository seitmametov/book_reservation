package com.example.library.service;

import com.example.library.repository.ConfirmationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenCleanupService {
    private final ConfirmationTokenRepository tokenRepository;

    // Запускать каждые 5 минут (300 000 миллисекунд)
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void removeExpiredTokens() {
        System.out.println("Дворник вышел на работу: чистим старые токены...");
        tokenRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());
    }
}