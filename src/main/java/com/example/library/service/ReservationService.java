package com.example.library.service;

import com.example.library.Dto.response.ReservationResponse;
import com.example.library.config.ReservationProperties;
import com.example.library.enam.BookEventType;
import com.example.library.enam.BookStatus;
import com.example.library.enam.NotificationType;
import com.example.library.enam.ReservationStatus;
import com.example.library.entity.Book;
import com.example.library.entity.Reservation;
import com.example.library.entity.User;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final ReservationProperties reservationProperties;
    private final BookHistoryService bookHistoryService;
    private final NotificationService notificationService;



    /**
     * Забронировать книгу
     */
    @Transactional // Обязательно добавь эту аннотацию!
    public ReservationResponse reserveBook(Long bookId, User user) {

        // 1. Проверка лимита активных книг
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

        // 2. Поиск и проверка доступности книги
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new RuntimeException("Book is not available");
        }

        // 3. Создание записи о бронировании
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setReservedAt(LocalDateTime.now());
        reservation.setStatus(ReservationStatus.ACTIVE); // Убедись, что ставишь начальный статус

        // Меняем статус книги
        book.setStatus(BookStatus.RESERVED);

        // 4. Сохранение в БД
        reservationRepository.save(reservation);
        bookRepository.save(book);

        // Логируем историю
        bookHistoryService.log(book, user, BookEventType.RESERVED);

        // ============================================================
        // 5. ОТПРАВКА УВЕДОМЛЕНИЯ (Интеграция по ТЗ)
        // ============================================================
        String title = "Книга забронирована: " + book.getTitle();
        String location = (book.getLocation() != null) ? book.getLocation() : "уточните на стойке";
        String message = "Заберите её в течение 24 часов. Локация: " + location;

        notificationService.createNotification(
                user,
                title,
                message,
                NotificationType.RESERVATION_SUCCESS
        );
        // ============================================================

        return toResponse(reservation);
    }

    /**
     * Вернуть книгу
     * @param reservationId
     * @param user
     */

    @Transactional
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
        reservation.setReturnedAt(LocalDateTime.now()); // Не забудь проставить дату возврата

        Book book = reservation.getBook();
        book.setStatus(BookStatus.AVAILABLE);

        reservationRepository.save(reservation);
        bookRepository.save(book);

        bookHistoryService.log(book, user, BookEventType.RETURNED);

        // УВЕДОМЛЕНИЕ: Книга возвращена
        notificationService.createNotification(
                user,
                "Книга возвращена",
                String.format("Спасибо! Книга '%s' успешно принята библиотекой.", book.getTitle()),
                NotificationType.RESERVATION_SUCCESS
        );
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

        bookHistoryService.log(book, user, BookEventType.TAKEN);

        notificationService.createNotification(
                user,
                "Книга выдана: " + book.getTitle(),
                "Вы успешно взяли книгу. Срок чтения — 14 дней. Пожалуйста, верните её вовремя!",
                NotificationType.RESERVATION_SUCCESS // Можно создать новый тип PICKUP_SUCCESS, если есть в enum
        );

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

        bookHistoryService.log(book, user, BookEventType.CANCELLED);

        notificationService.createNotification(
                user,
                "Бронирование отменено",
                "Вы отменили бронь на книгу '" + book.getTitle() + "'.",
                NotificationType.RESERVATION_CANCELLED
        );



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
    public List<ReservationResponse> getOverdueReservations() {

        LocalDateTime overdueDate = LocalDateTime.now().minusDays(14);

        return reservationRepository.findOverdue(overdueDate)
                .stream()
                .map(r -> new ReservationResponse(
                        r.getId(),
                        r.getBook().getId(),
                        r.getBook().getTitle(),
                        r.getReservedAt(),
                        r.getTakenAt(),
                        r.getReturnedAt(),
                        r.getStatus()
                ))
                .toList();
    }
    /**
     * Получить все бронирования для админ-панели
     */
    public List<ReservationResponse> getAllReservationsForAdmin(ReservationStatus status) {
        List<Reservation> reservations;

        if (status != null) {
            reservations = reservationRepository.findByStatus(status);
        } else {
            reservations = reservationRepository.findAll();
        }

        return reservations.stream()
                .map(this::toResponse)
                .toList();
    }


}

