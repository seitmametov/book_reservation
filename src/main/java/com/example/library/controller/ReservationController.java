package com.example.library.controller;

import com.example.library.Dto.ReservationResponse;
import com.example.library.entity.User;
import com.example.library.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/{bookId}")
    public ReservationResponse reserve(
            @PathVariable Long bookId,
            @AuthenticationPrincipal User user
    ) {
        return reservationService.reserveBook(bookId, user);
    }

    @PostMapping("/{id}/take")
    public void take(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        reservationService.takeBook(id, user);
    }
    @PostMapping("/{id}/return")
    public void returnBook(@PathVariable Long id,
                           @AuthenticationPrincipal User user) {
        reservationService.returnBook(id, user);
    }

    @GetMapping("/my")
    public List<ReservationResponse> my(@AuthenticationPrincipal User user) {
        return reservationService.myReservations(user);
    }
    @GetMapping("/my/active")
    public List<ReservationResponse> myActive(
            @AuthenticationPrincipal User user) {
        return reservationService.myActiveReservations(user);
    }


    @DeleteMapping("/{id}")
        public void cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    )

    {
        reservationService.cancel(id, user);
    }
}

