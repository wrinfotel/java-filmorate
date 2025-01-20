package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;

    private final UserStorage userStorage;

    public void addLike(Long filmId, Long userId) {
        User user = userStorage.findById(userId);

        Film film = filmStorage.findById(filmId);

        user.getLikedFilms().add(film.getId());
        film.addLike();
    }

    public void removeLike(Long filmId, Long userId) {
        User user = userStorage.findById(userId);
        Film film = filmStorage.findById(filmId);
        Set<Long> likedFilms = user.getLikedFilms();
        if (!likedFilms.isEmpty()) {
            likedFilms.remove(film.getId());
        }
        if (film.getLikesCount() > 0) {
            film.removeLike();
        }
    }

    public List<Film> getTopFilms(Integer count) {
        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> Long.compare(f2.getLikesCount(), f1.getLikesCount()))
                .limit(count).collect(Collectors.toList());
    }

}
