package com.example.library.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ConfirmationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public ConfirmationToken(User user, int minutes) {
        this.user = user;
        this.token = java.util.UUID.randomUUID().toString();
        this.expiresAt = LocalDateTime.now().plusMinutes(minutes); // Устанавливаем срок
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt); // Если СЕЙЧАС больше, чем ВРЕМЯ ИСТЕЧЕНИЯ
    }
}