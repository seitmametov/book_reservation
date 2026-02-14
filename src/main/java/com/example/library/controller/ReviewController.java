package com.example.library.controller;

import com.example.library.Dto.request.ReviewRequest;
import com.example.library.Dto.response.ReviewResponse;
import com.example.library.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "Оставить отзыв (1-5 звезд)")
    @PostMapping("/{id}/reviews")
    @SecurityRequirement(name = "bearerAuth")
    public ReviewResponse addReview(
            @PathVariable Long id,
            @RequestBody @Valid ReviewRequest request,
            Principal principal // Spring сам достанет email из JWT
    ) {
        return reviewService.addReview(id, request, principal.getName());
    }

    @Operation(summary = "Получить все отзывы книги")
    @GetMapping("/{id}/reviews")
    public List<ReviewResponse> getReviews(@PathVariable Long id) {
        return reviewService.getBookReviews(id);
    }

    @Operation(summary = "Редактировать свой отзыв")
    @PutMapping("/{reviewId}")
    public ReviewResponse updateReview(
            @PathVariable Long reviewId,
            @RequestBody @Valid ReviewRequest request,
            Principal principal
    ) {
        // Редактировать может только владелец (проверка внутри сервиса)
        return reviewService.updateReview(reviewId, request, principal.getName());
    }

    @Operation(summary = "Удалить отзыв (автор или админ)")
    @DeleteMapping("/{reviewId}")
    public void deleteReview(
            @PathVariable Long reviewId,
            Principal principal
    ) {
        // Проверяем роль через SecurityContext
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        reviewService.deleteReview(reviewId, principal.getName(), isAdmin);
    }
}
