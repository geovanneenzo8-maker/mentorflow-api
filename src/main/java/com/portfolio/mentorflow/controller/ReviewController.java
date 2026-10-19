package com.portfolio.mentorflow.controller;

import com.portfolio.mentorflow.dto.ReviewCreateRequest;
import com.portfolio.mentorflow.dto.ReviewResponse;
import com.portfolio.mentorflow.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "4. Avaliações & Reputação", description = "Avaliação de sessões concluídas com recálculo automático da nota média do mentor")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/mentor/{mentorId}")
    @Operation(summary = "Listar avaliações de um mentor", description = "Retorna todas as avaliações verificadas recebidas por um mentor.")
    public List<ReviewResponse> listReviewsByMentor(@PathVariable Long mentorId) {
        return reviewService.listReviewsByMentor(mentorId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Avaliar sessão concluída",
            description = "Registra nota (1 a 5) e depoimento para uma mentoria COMPLETED e recalcula a reputação do mentor."
    )
    public ReviewResponse createReview(@Valid @RequestBody ReviewCreateRequest request) {
        return reviewService.createReview(request);
    }
}
