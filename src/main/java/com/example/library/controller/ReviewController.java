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
}
