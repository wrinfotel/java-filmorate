package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = null;
        do {
            if (film == null) {
                film = Film.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .releaseDate(rs.getDate("release_date").toLocalDate())
                        .duration(rs.getInt("duration"))
                        .likesCount(rs.getInt("likes_count"))
                        .genres(new ArrayList<>())
                        .build();

                MpaRating mpaRating = MpaRating.builder()
                        .id(rs.getLong("mpa_id"))
                        .name(rs.getString("mpa_name"))
                        .build();
                film.setMpa(mpaRating);
            }
            if (rs.getLong("genre_id") != 0) {
                Genre genre = Genre.builder()
                        .id(rs.getLong("genre_id"))
                        .name(rs.getString("genre_name"))
                        .build();
                if (!film.getGenres().contains(genre)) {
                    film.getGenres().add(genre);
                }
            }
        } while (rs.next());

        return film;
    }
}
