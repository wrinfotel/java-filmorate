package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.Feed;
import ru.yandex.practicum.filmorate.model.feed.Operation;
import ru.yandex.practicum.filmorate.storage.mapper.FeedRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FeedRowMapper feedRowMapper;

    @Autowired
    public FeedDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.feedRowMapper = new FeedRowMapper();
    }

    @Override
    public Feed getById(Long id) {
        String getById = """
            SELECT *
            FROM "feed" WHERE id = ?
            """;
        try {
            return jdbcTemplate.queryForObject(getById, feedRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            log.error("Event with id {} not found", id);
            throw new NotFoundException("Event with id = " + id + " not found");
        }
    }

    @Override
    public Feed create(Long userId, EventType event, Operation operation, Long entityId) {
        log.info("Creating new event: userId={}, event={}, operation={}, entityId={}",
                userId, event, operation, entityId);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        String createFeed = """
            INSERT INTO "feed"(entity_id, user_id, time_stamp, event_type, operation)
            VALUES (?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(createFeed, Statement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, entityId);
            stmt.setLong(2, userId);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.setString(4, event.toString());
            stmt.setString(5, operation.toString());
            return stmt;
        }, keyHolder);

        Long feedId = Optional.ofNullable(keyHolder.getKey()).map(Number::longValue)
                .orElseThrow(() -> {
                    log.error("Error adding user {} to the feed table", userId);
                    return new NotFoundException("Error adding user to the feed table");
                });

        log.info("New event created with id {}", feedId);
        return getById(feedId);
    }

    @Override
    public List<Feed> getUserFeed(Long userId) {
        String getUserFeed = """
                SELECT *
                FROM "feed" WHERE user_id = ?
                ORDER BY time_stamp
                """;

        List<Feed> result = jdbcTemplate.query(getUserFeed, feedRowMapper, userId);
        log.info("Returning user feed for userId={}: {}", userId, result);
        return result;
    }
}