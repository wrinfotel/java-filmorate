package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.Operation;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;

    private final FeedStorage feedStorage;

    public ReviewService(ReviewStorage reviewStorage, FeedStorage feedStorage) {
        this.reviewStorage = reviewStorage;
        this.feedStorage = feedStorage;
    }

    public ReviewDto createReview(Review review) {
        if (review.getUserId() <= 0) {
            throw new NotFoundException("User not found.");
        }
        if (review.getFilmId() <= 0) {
            throw new NotFoundException("Film not found.");
        }
        Review reviewCreated = reviewStorage.createReview(review);
        feedStorage.create(review.getUserId(), EventType.REVIEW, Operation.ADD, reviewCreated.getReviewId());
        return ReviewMapper.mapToReviewDto(reviewCreated);
    }

    public ReviewDto updateReview(Review newReview) {
        if (newReview.getReviewId() == null) {
            throw new ValidationException("Id must be specified");
        }

        Review oldReview = reviewStorage.getReviewById(newReview.getReviewId())
                .orElseThrow(() -> new NotFoundException("Review with id = " + newReview.getReviewId() + "was not found"));

        Review updatedReview = reviewStorage.updateReview(updateReviewFields(oldReview, newReview));
        feedStorage.create(updatedReview.getUserId(), EventType.REVIEW, Operation.UPDATE, updatedReview.getReviewId());

        return ReviewMapper.mapToReviewDto(updatedReview);
    }

    public void deleteReview(Long id) {
        Review review = getReviewById(id);
        try {
            reviewStorage.deleteReview(id);
            feedStorage.create(review.getUserId(), EventType.REVIEW, Operation.REMOVE, review.getReviewId());
        } catch (Exception e) {
            throw new RuntimeException("Error while deleting. " + e.getMessage());
        }
    }

    public Review getReviewById(Long id) {
        return reviewStorage.getReviewById(id)
            .orElseThrow(() -> new NotFoundException("Review with id " + id + "was not found"));
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

    private Review updateReviewFields(Review oldReview, Review newReview) {
        oldReview.setContent(newReview.getContent());
        oldReview.setIsPositive(newReview.getIsPositive());
        return oldReview;
    }
}
