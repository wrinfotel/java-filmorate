package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreStorage genresStorage;

    public Collection<Genre> findAll() {
        return genresStorage.findAll();
    }

    public Genre findById(Long id) {
        return genresStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден"));
    }
}
