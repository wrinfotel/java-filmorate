package ru.yandex.practicum.filmorate.storage.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.Feed;
import ru.yandex.practicum.filmorate.model.feed.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class FeedRowMapper implements RowMapper<Feed> {

    public Feed mapRow(ResultSet rs, int rowNum) throws SQLException {
        String event = rs.getString("event_type").toUpperCase();
        String operation = rs.getString("operation").toUpperCase();

        return Feed.builder()
                .eventId(rs.getLong("id"))
                .timestamp(rs.getLong("time_stamp"))
                .userId(rs.getLong("user_id"))
                .eventType(checkValidEvent(event))
                .operation(checkValidOperation(operation))
                .entityId(rs.getLong("entity_id"))
                .build();
    }

    private EventType checkValidEvent(String event) {
        return switch (event) {
            case ("LIKE") -> EventType.LIKE;
            case ("REVIEW") -> EventType.REVIEW;
            case ("FRIEND") -> EventType.FRIEND;
            default -> throw new NotFoundException("EventType was not found");
        };
    }

    private Operation checkValidOperation(String operation) {
        return switch (operation) {
            case ("REMOVE") -> Operation.REMOVE;
            case ("ADD") -> Operation.ADD;
            case ("UPDATE") -> Operation.UPDATE;
            default -> throw new NotFoundException("Operation was not found");
        };
    }
}