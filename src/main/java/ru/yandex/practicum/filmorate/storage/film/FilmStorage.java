package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Collection<Film> findAll();

    Optional<Film> findById(long id);

    Film create(Film film);

    Film update(Film newFilm);

    void addLike(Film film, User user);

    boolean removeLike(Film film, User user);

    Collection<Film> findFilmsByDirector(Director director, String sortField);

    List<Film> getCommonFilms(Long userId, Long friendId);

    boolean deleteById(long id);

    List<Film> search(String query, String searchBy);

    List<Film> getRecommendations(Long userId);

    Collection<Film> getPopularFilm(Integer count, Integer genreId, Integer year);
}
