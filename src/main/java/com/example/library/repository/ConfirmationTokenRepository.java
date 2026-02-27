package com.example.library.repository;

import com.example.library.entity.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {
    Optional<ConfirmationToken> findByToken(String token);

    void deleteAllByExpiresAtBefore(LocalDateTime now);

    @Modifying
    @Transactional
    @Query("DELETE FROM ConfirmationToken t WHERE t.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}