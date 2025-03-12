package ru.yandex.practicum.filmorate.model.feed;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Feed {
    private Long timestamp;

    private Long userId;

    private EventType eventType;

    private Operation operation;

    private Long eventId;

    private Long entityId;
}
