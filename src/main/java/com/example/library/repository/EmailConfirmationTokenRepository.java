package com.example.library.repository;

import com.example.library.entity.EmailConfirmationToken;
import com.google.common.base.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailConfirmationTokenRepository
        extends JpaRepository<EmailConfirmationToken, Long> {

    Optional<EmailConfirmationToken> findByToken(String token);
}
