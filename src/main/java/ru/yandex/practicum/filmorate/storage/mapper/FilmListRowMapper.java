package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
public class FilmListRowMapper implements RowMapper<List<Film>> {

    @Override
    public List<Film> mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<Long, Film> filmsMap = new HashMap<>();
        do {
            Long filmId = rs.getLong("id");
            Film film = filmsMap.get(filmId);
            if (film == null) {
                film = Film.builder()
                        .id(filmId)
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .releaseDate(Objects.requireNonNull(rs.getDate("release_date")).toLocalDate())
                        .duration(rs.getInt("duration"))
                        .likesCount(rs.getInt("likes_count"))
                        .genres(new ArrayList<>())
                        .build();
                if (rs.getLong("MPA_ID") != 0) {
                    MpaRating mpaRating = MpaRating.builder()
                            .id(rs.getLong("MPA_ID"))
                            .name(rs.getString("MPA_NAME"))
                            .build();
                    film.setMpa(mpaRating);
                }
                filmsMap.put(film.getId(), film);
            }

            if (rs.getLong("GENRE_ID") != 0) {
                Genre genre = Genre.builder()
                        .id(rs.getLong("GENRE_ID"))
                        .name(rs.getString("GENRE_NAME"))
                        .build();
                if (!film.getGenres().contains(genre)) {
                    film.getGenres().add(genre);
                }
            }
        } while (rs.next());

        return filmsMap.values().stream().toList();
    }
}
