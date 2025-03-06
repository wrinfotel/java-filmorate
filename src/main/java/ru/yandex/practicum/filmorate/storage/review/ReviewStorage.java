package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Review createReview(Review reviews);

    Review updateReview(Review reviews);

    void deleteReview(Long id);

    Optional<Review> getReviewById(Long id);

    List<Review> getReviewByFilm(Long id, int count);

    List<Review> getAllReviews(int count);

    void likeOrDislikeToReview(Long reviewId, Long userId, boolean isLike);

    void deleteLikeOrDislike(Long reviewId, Long userId, boolean isLike);
}
