package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto createReview(@Valid @RequestBody Review review) {
        return reviewService.createReview(review);
    }

    @PutMapping
    public ReviewDto updateReview(@Valid @RequestBody Review review) {
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public List<Review> getReviewByFilm(@RequestParam(required = false) Long filmId,
                                        @RequestParam(defaultValue = "10") int count) {
        if (filmId != null) {
            return reviewService.getReviewByFilm(filmId, count);
        } else {
            return reviewService.getAllReviews(count);
        }
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeToReview(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.likeToReview(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislikeToReview(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.dislikeToReview(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.deleteLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.deleteDislike(id, userId);
    }
}
