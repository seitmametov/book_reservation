package com.example.library.service;

import com.example.library.Dto.ReservationResponse;
import com.example.library.config.ReservationProperties;
import com.example.library.enam.BookStatus;
import com.example.library.enam.ReservationStatus;
import com.example.library.entity.Book;
import com.example.library.entity.Reservation;
import com.example.library.entity.User;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final ReservationProperties reservationProperties;

    /**
     * Забронировать книгу
     */
    public ReservationResponse reserveBook(Long bookId, User user) {

        long activeCount = reservationRepository.countByUserAndStatusIn(
                user,
                List.of(
                        ReservationStatus.ACTIVE,
                        ReservationStatus.COMPLETED
                )
        );

        if (activeCount >= reservationProperties.getMaxActive()) {
            throw new RuntimeException(
                    "You have reached the maximum number of reserved books"
            );
        }


        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new RuntimeException("Book is not available");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setReservedAt(LocalDateTime.now());

        book.setStatus(BookStatus.RESERVED);

        reservationRepository.save(reservation);
        bookRepository.save(book);

        return toResponse(reservation);
    }

    /**
     * Вернуть книгу
     * @param reservationId
     * @param user
     */

    public void returnBook(Long reservationId, User user) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You cannot return someone else's book");
        }

        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new RuntimeException("Book was not taken");
        }

        reservation.setStatus(ReservationStatus.RETURNED);

        Book book = reservation.getBook();
        book.setStatus(BookStatus.AVAILABLE);

        reservationRepository.save(reservation);
        bookRepository.save(book);
    }


    /**
     *     Проверка естьли у пользователя активные брони
     */
    public List<ReservationResponse> myActiveReservations(User user) {
        return reservationRepository.findByUserAndStatusIn(
                        user,
                        List.of(
                                ReservationStatus.ACTIVE,
                                ReservationStatus.COMPLETED
                        )
                ).stream()
                .map(this::toResponse)
                .toList();
    }



    /**
     * Пользователь забрал книгу
     */
    public void takeBook(Long reservationId, User user) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not your reservation");
        }

        Book book = reservation.getBook();
        book.setStatus(BookStatus.TAKEN);

        // ВМЕСТО delete(reservation) делаем фиксацию:
        reservation.setStatus(ReservationStatus.COMPLETED); // Помечаем, что на руках
        reservation.setTakenAt(LocalDateTime.now());       // Засекаем время выдачи

        reservationRepository.save(reservation); // СОХРАНЯЕМ, а не удаляем
        bookRepository.save(book);
    }

    /**
     * Отмена брони
     */
    public void cancel(Long reservationId, User user) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // Проверка юзера остается...

        Book book = reservation.getBook();
        book.setStatus(BookStatus.AVAILABLE);

        // ВМЕСТО delete — помечаем как отмененную
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        bookRepository.save(book);
    }

    public List<ReservationResponse> myReservations(User user) {
        return reservationRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getBook().getId(),
                reservation.getBook().getTitle(),
                reservation.getReservedAt(),
                reservation.getTakenAt(),    // Передаем дату взятия
                reservation.getReturnedAt(),  // Передаем дату возврата
                reservation.getStatus()      // Передаем статус
        );
    }
    public List<ReservationResponse> getMyReadingHistory(User user) {
        // Просто тянем всё, где статус RETURNED (прочитано)
        return reservationRepository.findByUserAndStatusIn(
                        user,
                        List.of(ReservationStatus.RETURNED)
                ).stream()
                .map(this::toResponse)
                .toList();
    }
}

