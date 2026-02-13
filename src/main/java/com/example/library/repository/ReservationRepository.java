package com.example.library.repository;

import com.example.library.enam.ReservationStatus;
import com.example.library.entity.Book;
import com.example.library.entity.Reservation;
import com.example.library.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUser(User user);

    List<Reservation> findByStatusAndExpiresAtBefore(
            ReservationStatus status,
            LocalDateTime time
    );

    long countByUserAndStatusIn(
            User user,
            List<ReservationStatus> statuses
    );

    List<Reservation> findByUserAndStatusIn(
            User user,
            List<ReservationStatus> statuses
    );
    @Query("""
        select r from Reservation r
        where r.status = 'TAKEN'
        and r.takenAt < :overdueDate
    """)
    List<Reservation> findOverdue(@Param("overdueDate") LocalDateTime overdueDate);

    List<Reservation> findAllByStatusAndReservedAtBetween(ReservationStatus status, LocalDateTime start, LocalDateTime end);
    List<Reservation> findAllByStatusAndReservedAtBefore(ReservationStatus status, LocalDateTime dateTime);
    List<Reservation> findAllByStatusAndTakenAtBetween(ReservationStatus status, LocalDateTime start, LocalDateTime end);
    List<Reservation> findAllByStatusAndTakenAtBefore(ReservationStatus status, LocalDateTime dateTime);

    List<Reservation> findByStatus(ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.book = :book AND r.status IN ('ACTIVE', 'COMPLETED')")
    Optional<Reservation> findActiveByBook(@Param("book") Book book);

}



