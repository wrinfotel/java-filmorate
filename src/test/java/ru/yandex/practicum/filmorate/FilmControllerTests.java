package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@SpringBootTest
public class FilmControllerTests {

    @Autowired
    private FilmController controller;

    @Test
    void shouldCreateFilm() {
        Film film = Film.builder()
                .name("TestFilm")
                .description("test Description")
                .releaseDate(LocalDate.parse("2011-05-12"))
                .duration(100)
                .build();

        Film createdFilm = controller.create(film);
        Assertions.assertEquals(film, createdFilm);
    }

    @Test
    void shouldNotCreateFilmWithoutName() {
        Film film = Film.builder()
                .description("test Description")
                .releaseDate(LocalDate.parse("2011-05-12"))
                .duration(100)
                .build();
        Assertions.assertThrows(ValidationException.class,
                () -> controller.create(film),
                "Название фильма должно быть заполнено - тест провален");
    }

    @Test
    void shouldNotCreateFilmWithLongDescription() {
        Film film = Film.builder()
                .name("TestFilm")
                .description("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque feugiat felis accumsan" +
                        " dui accumsan maximus. Aenean maximus ullamcorper turpis sed mollis. Curabitur quis " +
                        "nulla iaculis, luctus turpisa.")
                .releaseDate(LocalDate.parse("2011-05-12"))
                .duration(100)
                .build();
        Assertions.assertThrows(ValidationException.class,
                () -> controller.create(film),
                "Описание не должно превышать 200 символов - тест провален");
    }

    @Test
    void shouldNotCreateFilmWithZeroDuration() {
        Film film = Film.builder()
                .name("TestFilm")
                .description("test Description")
                .releaseDate(LocalDate.parse("2011-05-12"))
                .duration(0)
                .build();
        Assertions.assertThrows(ValidationException.class,
                () -> controller.create(film),
                "Продолжительность должна быть больше нуля - тест провален");
    }

    @Test
    void shouldNotCreateFilmWithOutOfReleaseDate() {
        Film film = Film.builder()
                .name("TestFilm")
                .description("test Description")
                .releaseDate(LocalDate.parse("1895-12-27"))
                .duration(100)
                .build();
        Assertions.assertThrows(ValidationException.class,
                () -> controller.create(film),
                "Дата релиза должна быть позже или равна 28 декабря 1895 - тест провален");
    }

    @Test
    void shouldNotUpdateFilm() {
        Film film = Film.builder()
                .id(55L)
                .name("TestFilm")
                .description("test Description")
                .releaseDate(LocalDate.parse("1900-12-27"))
                .duration(100)
                .build();
        Assertions.assertThrows(NotFoundException.class,
                () -> controller.update(film),
                "Фильм не найден - тест провален");
    }

    @Test
    void shouldNotUpdateFilmWithoutId() {
        Film film = Film.builder()
                .name("TestFilm")
                .description("test Description")
                .releaseDate(LocalDate.parse("1900-12-27"))
                .duration(100)
                .build();
        Assertions.assertThrows(ValidationException.class,
                () -> controller.update(film),
                "Обновление без id - тест провален");
    }

    @Test
    void shouldUpdateFilm() {
        Film film = Film.builder()
                .name("TestFilm")
                .description("test Description")
                .releaseDate(LocalDate.parse("1900-12-27"))
                .duration(100)
                .build();
        Film createdFilm = controller.create(film);

        Film filmUpdate = Film.builder()
                .id(createdFilm.getId())
                .name("TestFilm Update")
                .description("test Description Update")
                .releaseDate(LocalDate.parse("1995-12-27"))
                .duration(100)
                .build();

        Film updateFilm = controller.update(filmUpdate);
        Assertions.assertEquals(filmUpdate, updateFilm);
    }
}
