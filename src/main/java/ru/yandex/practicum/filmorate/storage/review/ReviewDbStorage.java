package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.mapper.ReviewRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Review> mapper;

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = new ReviewRowMapper();
    }

    @Override
    public Review createReview(Review review) {
        final String ADD_REVIEW_QUERY = """
            INSERT INTO "reviews" (content, is_positive, user_id, film_id)
            VALUES (?, ?, ?, ?)
            """;

        long reviewId;
        boolean isPositive = review.getIsPositive() != null ? review.getIsPositive() : false;
        KeyHolder key = new GeneratedKeyHolder();
        jdbcTemplate.update(m -> {
            PreparedStatement ps = m.prepareStatement(ADD_REVIEW_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            return ps;
        }, key);

        if (Objects.isNull(key.getKey())) {
            throw new ValidationException("Error assigning an id to a review");
        }

        reviewId = key.getKey().longValue();
        review.setReviewId(reviewId);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        final String UPDATE_REVIEW_QUERY = """
            UPDATE "reviews" SET content = ?, is_positive = ?
            WHERE id = ?
            """;

        int update = jdbcTemplate.update(UPDATE_REVIEW_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );
        if (update == 0) {
            log.info("Failed to update review with id {}.", review.getReviewId());
            throw new NotFoundException("No review with this id was found.");
        }

        return review;
    }

    @Override
    public void deleteReview(Long id) {
        final String DELETE_REVIEW_QUERY = """
            DELETE FROM "reviews" WHERE id = ?
            """;
        final String DELETE_USEFUL_QUERY = """
            DELETE FROM "useful" WHERE review_id = ?
            """;
        jdbcTemplate.update(DELETE_USEFUL_QUERY, id);
        jdbcTemplate.update(DELETE_REVIEW_QUERY, id);
    }

    @Override
    public Optional<Review> getReviewById(Long id) {
        try {
        final String QUERY = """
            SELECT r.id, r.content, r.is_positive, u.name AS user_name, f.name AS film_name,
               r.user_id, r.film_id,
               COALESCE(SUM(CASE WHEN uf.is_like IS TRUE THEN 1 ELSE 0 END), 0) AS likes,
               COALESCE(SUM(CASE WHEN uf.is_like IS FALSE THEN 1 ELSE 0 END), 0) AS dislikes
            FROM "reviews" r
            JOIN "user" u ON r.user_id = u.id
            JOIN "film" f ON r.film_id = f.id
            LEFT JOIN "useful" uf ON r.id = uf.review_id
            WHERE r.id = ?
            GROUP BY r.id, r.content, r.is_positive, u.name, f.name, r.user_id, r.film_id;
            """;
        Review review = jdbcTemplate.queryForObject(QUERY, mapper, id);
        return Optional.ofNullable(review);
        } catch (
        EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> getReviewByFilm(Long id, int count) {
        final String GET_REVIEW_BY_FILM = """
            SELECT r.id, r.content, r.is_positive, r.user_id, r.film_id,
                    COALESCE(likes.lik, 0) AS likes,
                    COALESCE(dislikes.dis, 0) AS dislikes
            FROM "reviews" r
            LEFT JOIN (
                    SELECT review_id, COUNT(*) AS lik
                    FROM "useful"
                    WHERE is_like = TRUE
                    GROUP BY review_id
            ) likes ON likes.review_id = r.id
                LEFT JOIN (
                    SELECT review_id, COUNT(*) AS dis
                    FROM "useful"
                    WHERE is_like = FALSE
                    GROUP BY review_id
                ) dislikes ON dislikes.review_id = r.id
                JOIN "film" f ON r.film_id = f.id
                WHERE r.film_id = ?
                ORDER BY (COALESCE(likes.lik, 0) - COALESCE(dislikes.dis, 0)) DESC
                LIMIT ?
            """;

        List<Review> reviews = jdbcTemplate.query(GET_REVIEW_BY_FILM, mapper, id, count);
        if (reviews == null || reviews.isEmpty()) {
            log.warn("No reviews found for filmId={}.", id);
        }
        log.info("Fetched reviews: {}", reviews);
        return reviews;
    }

    @Override
    public List<Review> getAllReviews(int count) {
        final String GET_ALL = """
            SELECT r.id, r.content, r.is_positive, r.user_id, r.film_id, likes.lik AS likes, dislikes.dis AS dislikes
            FROM "reviews" r
            LEFT JOIN (SELECT review_id, COUNT(*) AS lik FROM "useful" WHERE is_like = TRUE
            GROUP BY review_id) likes
            ON likes.review_id = r.id
            LEFT JOIN (SELECT review_id, COUNT(*) AS dis FROM "useful" WHERE is_like = FALSE
            GROUP BY review_id) dislikes
            ON dislikes.review_id = r.id
            ORDER BY (COALESCE(likes.lik, 0) - COALESCE(dislikes.dis, 0)) DESC
            LIMIT ?;
            """;

        List<Review> reviews = jdbcTemplate.query(GET_ALL, mapper, count);
        log.info("Get review of film {}.", reviews);
        return reviews;
    }

    @Override
    public void likeOrDislikeToReview(Long reviewId, Long userId, boolean isLike) {
        String checkSql = """
            SELECT * FROM "useful" WHERE review_id = ? AND user_id = ?
            """;
        List<Map<String, Object>> likeDislike = jdbcTemplate.queryForList(checkSql, reviewId, userId);

        if (!likeDislike.isEmpty()) {
            String updateSql = """
                UPDATE "useful" SET is_like = ? WHERE review_id = ? AND user_id = ?
                """;
            jdbcTemplate.update(updateSql, isLike, reviewId, userId);
        } else {
            String insertSql = """
                INSERT INTO "useful" (review_id, is_like, user_id) VALUES (?, ?, ?)
                """;
            jdbcTemplate.update(insertSql, reviewId, isLike, userId);
        }
        log.info("Add {} to review {} or update for user {}.", isLike ? "like" : "dislike", reviewId, userId);
    }

    @Override
    public void deleteLikeOrDislike(Long reviewId, Long userId, boolean isLike) {
        final String DELETE_LIKE_OR_DISLIKE = """
            DELETE FROM "useful" WHERE review_id = ? AND user_id = ?
            """;
        String action = isLike ? "Like" : "dislike";
        jdbcTemplate.update(DELETE_LIKE_OR_DISLIKE, reviewId, userId);
        log.info(action + " deleted.");
    }
}
