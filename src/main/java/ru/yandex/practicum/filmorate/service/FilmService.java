package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    @Autowired
    @Qualifier("filmDbStorage")
    private FilmStorage filmStorage;

    @Autowired
    private UserService userService;

    @Autowired
    private MpaService mpaService;

    @Autowired
    private GenreService genreService;

    private final Logger log = LoggerFactory.getLogger(FilmService.class);

    public Collection<FilmDto> findAll() {
        return filmStorage.findAll().stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public Film findById(Long id) {
        return filmStorage.findOne(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }

    public FilmDto findFilmById(Long id) {
        return filmStorage.findOne(id).map(FilmMapper::mapToFilmDto)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }

    public FilmDto create(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Ошибка валидации - Дата релиза должна быть позже или равна 28 декабря 1895");
            throw new ValidationException("Дата релиза должна быть позже или равна 28 декабря 1895");
        }
        mpaService.findById(film.getMpa().getId());
        checkGenres(film.getGenres());
        Film createdFilm = filmStorage.create(film);
        log.info("New film added " + film.getId());
        return FilmMapper.mapToFilmDto(createdFilm);
    }

    private void checkGenres(List<Genre> genres) {
        if (genres != null) {
            for (Genre genre : genres) {
                genreService.findById(genre.getId());
            }
        }
    }

    public FilmDto update(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        if (newFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Ошибка валидации - Дата релиза должна быть позже или равна 28 декабря 1895");
            throw new ValidationException("Дата релиза должна быть позже или равна 28 декабря 1895");
        }
        Film oldFilm = filmStorage.findOne(newFilm.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден"));
        checkGenres(newFilm.getGenres());
        Film updatedFilm = filmStorage.update(FilmMapper.updateFilmFields(oldFilm, newFilm));
        log.info("Film updated " + updatedFilm.getId());
        return FilmMapper.mapToFilmDto(updatedFilm);
    }

    public void addLike(Long filmId, Long userId) {
        User user = userService.findById(userId);
        Film film = findById(filmId);
        filmStorage.addLike(film, user);
    }


    public void removeLike(Long filmId, Long userId) {
        User user = userService.findById(userId);
        Film film = findById(filmId);
        filmStorage.removeLike(film, user);
    }

    public List<FilmDto> getTopFilms(Integer count) {
        return findAll().stream()
                .sorted((f1, f2) -> Long.compare(f2.getLikesCount(), f1.getLikesCount()))
                .limit(count).collect(Collectors.toList());
    }

}
