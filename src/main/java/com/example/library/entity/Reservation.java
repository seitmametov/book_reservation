package com.example.library.entity;

import com.example.library.enam.ReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;   // тот самый сотрудник

    @ManyToOne(optional = false)
    private Book book;

    private LocalDateTime reservedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime takenAt;

    //===================
    private LocalDateTime returnedAt;
    //==========

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
}


