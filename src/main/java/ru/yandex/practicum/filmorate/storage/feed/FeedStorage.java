package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.Feed;
import ru.yandex.practicum.filmorate.model.feed.Operation;

import java.util.List;

public interface FeedStorage {
    Feed getById(Long id);

    Feed create(Long userId, EventType event, Operation operation, Long entityId);

    List<Feed> getUserFeed(Long id);
}