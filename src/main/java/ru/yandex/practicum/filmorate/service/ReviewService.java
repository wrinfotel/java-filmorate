package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;

    public ReviewService(ReviewStorage reviewStorage) {
        this.reviewStorage = reviewStorage;
    }

    public Review createReview(Review review) {
        if (review.getUserId() <= 0) {
            throw new NotFoundException("User not found.");
        }
        if (review.getFilmId() <= 0) {
            throw new NotFoundException("Film not found.");
        }
        return reviewStorage.createReview(review);
    }

    public ReviewDto updateReview(Review newReview) {
        if (newReview.getReviewId() == null) {
            throw new ValidationException("Id must be specified");
        }

        Review oldReview = reviewStorage.getReviewById(newReview.getReviewId()).
                orElseThrow(() -> new NotFoundException("Review with id = " + newReview.getReviewId() + "was not found"));

        Review updatedReview = reviewStorage.updateReview(ReviewMapper.updateReviewFields(oldReview, newReview));

        return ReviewMapper.mapToReviewDto(updatedReview);
    }

    public void deleteReview(Long id) {
        reviewStorage.getReviewById(id)
                .orElseThrow(() -> new NotFoundException("Review with id = " + id + " was not found"));

        try {
            reviewStorage.deleteReview(id);
        } catch (Exception e) {
            throw new RuntimeException("Error while deleting. " + e.getMessage());
        }
    }

    public Review getReviewById(Long id) {
        return reviewStorage.getReviewById(id).
                orElseThrow(() -> new NotFoundException("Review with id " + id + "was not found"));
    }

    public List<Review> getReviewByFilm(Long id, int count) {
        try {
            return reviewStorage.getReviewByFilm(id, count);
        } catch (Exception e) {
            throw new RuntimeException("Error while getting reviews for the film. " + e.getMessage());
        }
    }

    public List<Review> getAllReviews(int count) {
        try {
            return reviewStorage.getAllReviews(count);
        } catch (Exception e) {
            throw new RuntimeException("Error while getting all reviews. " + e.getMessage());
        }
    }

    public void likeToReview(Long reviewId, Long userId) {
        try {
            reviewStorage.likeOrDislikeToReview(reviewId, userId, true);
        } catch (Exception e) {
            throw new RuntimeException("Error while adding like to the review. " + e.getMessage());
        }
    }

    public void dislikeToReview(Long reviewId, Long userId) {
        try {
            reviewStorage.likeOrDislikeToReview(reviewId, userId, false);
        } catch (Exception e) {
            throw new RuntimeException("Error while adding dislike to the review. " + e.getMessage());
        }
    }

    public void deleteLike(Long reviewId, Long userId) {
        reviewStorage.deleteLikeOrDislike(reviewId, userId, true);
    }

    public void deleteDislike(Long reviewId, Long userId) {
        reviewStorage.deleteLikeOrDislike(reviewId, userId, false);
    }
}
