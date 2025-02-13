package ru.yandex.practicum.filmorate.dbTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaDbStorage.class})
class MpaDbStorageTests {

    private final MpaDbStorage mpaStorage;


    @Test
    public void testFindMpaById() {
        Optional<MpaRating> genreOptional = mpaStorage.findById(1);

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(mpa ->
                        assertThat(mpa).hasFieldOrPropertyWithValue("name", "G")
                );
    }

    @Test
    public void testGetAllGenres() {

        Collection<MpaRating> genres = mpaStorage.findAll();
        Assertions.assertEquals(5, genres.size());
    }


}