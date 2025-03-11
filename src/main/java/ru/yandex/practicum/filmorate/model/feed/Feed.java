package ru.yandex.practicum.filmorate.model.feed;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Feed {
    Long timestamp;

    Long userId;

    EventType eventType;

    Operation operation;

    Long eventId;

    Long entityId;
}
