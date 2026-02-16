    package com.example.library.config;

import com.example.library.enam.BookStatus;
import com.example.library.entity.Reservation;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;

    @Scheduled(cron = "0 0 * * * *")
    public void clearExpiredReservations() {
        List<Reservation> reservations = reservationRepository.findAll();

        for (Reservation r : reservations) {
            if (r.getReservedAt().isBefore(LocalDateTime.now().minusHours(24))) {
                r.getBook().setStatus(BookStatus.AVAILABLE);
                bookRepository.save(r.getBook());
                reservationRepository.delete(r);
            }
        }
    }

}
