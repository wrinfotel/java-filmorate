package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

public interface DirectorStorage {
    Collection<Director> findAll();

    Optional<Director> findById(long id);

    Director create(Director director);

    Director update(Director newDirector);

    boolean delete(Director director);
}
