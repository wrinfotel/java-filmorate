package ru.yandex.practicum.filmorate.dbTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class})
class FilmDbStorageTests {

    private final FilmDbStorage filmStorage;

    Genre genre = Genre.builder()
            .id(1L).build();
    private final Film newFilm = Film.builder()
            .name("Test")
            .description("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
            .releaseDate(LocalDate.parse("2011-05-12"))
            .duration(100)
            .mpa(MpaRating.builder().id(1).build())
            .genres(List.of(genre))
            .build();

    private final Film newFilm2 = Film.builder()
            .name("Test2")
            .description("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
            .releaseDate(LocalDate.parse("2005-05-12"))
            .duration(120)
            .mpa(MpaRating.builder().id(2).build())
            .genres(List.of(genre))
            .build();

    @Test
    public void testCreateFilm() {
        Film filmOptional = filmStorage.create(newFilm);
        Assertions.assertNotNull(filmOptional.getId());
        Collection<Film> films = filmStorage.findAll();
        Assertions.assertEquals(1, films.size());
    }


    @Test
    public void testFindFilmById() {
        Film created = filmStorage.create(newFilm);
        Optional<Film> filmOptional = filmStorage.findOne(created.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", created.getId())
                );
    }

    @Test
    public void testGetAllFilms() {
        filmStorage.create(newFilm);
        filmStorage.create(newFilm2);

        Collection<Film> films = filmStorage.findAll();
        Assertions.assertEquals(2, films.size());
    }

    @Test
    public void testUpdateFilm() {
        Film filmCreated = filmStorage.create(newFilm);
        filmCreated.setName("TestFilmUpdated");
        filmStorage.update(filmCreated);
        Optional<Film> updated = filmStorage.findOne(filmCreated.getId());

        assertThat(updated)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("name", "TestFilmUpdated")
                );
    }


}