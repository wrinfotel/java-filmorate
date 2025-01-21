package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;

    private final UserService userService;

    private final Logger log = LoggerFactory.getLogger(FilmService.class);

    public Collection<Film> findAll() {
        return filmStorage.findAll().values();
    }

    public Film findById(Long id) {
        if (!filmStorage.findAll().containsKey(id)) {
            log.error("Фильм с id = " + id + " не найден");
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        return filmStorage.findAll().get(id);
    }

    public void addLike(Long filmId, Long userId) {
        User user = userService.findById(userId);

        Film film = findById(filmId);

        user.getLikedFilms().add(film.getId());
        film.addLike();
    }

    public Film create(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Ошибка валидации - Дата релиза должна быть позже или равна 28 декабря 1895");
            throw new ValidationException("Дата релиза должна быть позже или равна 28 декабря 1895");
        }
        Film createdFilm = filmStorage.create(film);
        log.info("New film added " + film.getId());
        return createdFilm;
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!filmStorage.findAll().containsKey(newFilm.getId())) {
            log.error("Фильм с id = " + newFilm.getId() + " не найден");
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }

        if (newFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Ошибка валидации - Дата релиза должна быть позже или равна 28 декабря 1895");
            throw new ValidationException("Дата релиза должна быть позже или равна 28 декабря 1895");
        }
        Film updatedFilm = filmStorage.update(newFilm);
        log.info("Film updated " + updatedFilm.getId());
        return updatedFilm;
    }

    public void removeLike(Long filmId, Long userId) {
        User user = userService.findById(userId);
        Film film = findById(filmId);
        Set<Long> likedFilms = user.getLikedFilms();
        if (!likedFilms.isEmpty()) {
            likedFilms.remove(film.getId());
        }
        if (film.getLikesCount() > 0) {
            film.removeLike();
        }
    }

    public List<Film> getTopFilms(Integer count) {
        return findAll().stream()
                .sorted((f1, f2) -> Long.compare(f2.getLikesCount(), f1.getLikesCount()))
                .limit(count).collect(Collectors.toList());
    }

}
