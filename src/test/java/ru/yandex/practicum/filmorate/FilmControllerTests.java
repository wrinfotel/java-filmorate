package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FilmControllerTests {

    @Autowired
    private FilmController controller;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateFilm() throws Exception {
        String json = "{\"name\": \"TestFilm\"," +
                "\"description\": \"test Description\"," +
                " \"duration\": 100, \"mpa\": {\"id\":1}, " +
                "\"releaseDate\": \"2011-05-12\"}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

    }

    @Test
    void shouldNotCreateFilmWithoutName() throws Exception {
        String json = "{\"description\": \"test Description\", \"duration\": 100," +
                "\"releaseDate\": \"2011-05-12\"}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(
                        result -> assertInstanceOf(MethodArgumentNotValidException.class,
                                result.getResolvedException()));
    }

    @Test
    void shouldNotCreateFilmWithLongDescription() throws Exception {
        String json = "{\"name\": \"TestFilm\"," +
                "\"description\": \"Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Quisque feugiat felis accumsan dui accumsan maximus." +
                " Aenean maximus ullamcorper turpis sed mollis. Curabitur quis nulla iaculis, luctus turpisa.\"," +
                " \"duration\": 100," +
                "\"releaseDate\": \"2011-05-12\"}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(
                        result -> assertInstanceOf(MethodArgumentNotValidException.class,
                                result.getResolvedException()));
    }

    @Test
    void shouldNotCreateFilmWithZeroDuration() throws Exception {
        String json = "{\"name\": \"TestFilm\"," +
                "\"description\": \"test Description\"," +
                " \"duration\": 0," +
                "\"releaseDate\": \"2011-05-12\"}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(
                        result -> assertInstanceOf(MethodArgumentNotValidException.class,
                                result.getResolvedException()));
    }

    @Test
    void shouldNotCreateFilmWithOutOfReleaseDate() {
        Film film = Film.builder()
                .name("TestFilm")
                .description("test Description")
                .releaseDate(LocalDate.parse("1895-12-27"))
                .duration(100)
                .build();
        assertThrows(ValidationException.class,
                () -> controller.create(film),
                "Дата релиза должна быть позже или равна 28 декабря 1895 - тест провален");
    }

    @Test
    void shouldNotUpdateFilm() {
        Film film = Film.builder()
                .id(51115L)
                .name("TestFilm")
                .description("test Description")
                .releaseDate(LocalDate.parse("1900-12-27"))
                .duration(100)
                .build();
        assertThrows(NotFoundException.class,
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
        assertThrows(ValidationException.class,
                () -> controller.update(film),
                "Обновление без id - тест провален");
    }

    @Test
    void shouldUpdateFilm() {
        MpaRating mpa = MpaRating.builder().id(1L).build();
        Film film = Film.builder()
                .name("TestFilm")
                .description("test Description")
                .releaseDate(LocalDate.parse("1900-12-27"))
                .duration(100)
                .mpa(mpa)
                .build();
        FilmDto createdFilm = controller.create(film);

        Film filmUpdate = Film.builder()
                .id(createdFilm.getId())
                .name("TestFilm Update")
                .description("test Description Update")
                .releaseDate(LocalDate.parse("1995-12-27"))
                .duration(100)
                .build();

        FilmDto updateFilm = controller.update(filmUpdate);
        Assertions.assertEquals(filmUpdate.getName(), updateFilm.getName());
        Assertions.assertEquals(filmUpdate.getDescription(), updateFilm.getDescription());
        Assertions.assertEquals(filmUpdate.getReleaseDate(), updateFilm.getReleaseDate());
    }
}
