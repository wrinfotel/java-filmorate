package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@Repository("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Film> mapper;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = new FilmRowMapper();
    }

    @Override
    public Collection<Film> findAll() {
        String sqlQuery = "SELECT fi.*, count(uf.film_id) as likes_count, mr.id as mpa_id, mr.name as mpa_name" +
                " FROM \"film\" as fi " +
                "LEFT JOIN \"user_films\" as uf ON fi.id = uf.film_id LEFT JOIN \"mpa_rating\" AS mr" +
                " ON fi.rating_id = mr.id GROUP BY fi.ID";
        List<Film> films = jdbcTemplate.query(sqlQuery, mapper);
        for (Film film : films) {
            getFilmGenres(film);
        }
        return films;
    }

    @Override
    public Optional<Film> findOne(long id) {
        try {
            String sqlQuery = "SELECT fi.*, count(uf.film_id) as likes_count, mr.id as mpa_id, mr.name as mpa_name" +
                    " FROM \"film\" as fi " +
                    "LEFT JOIN \"user_films\" as uf ON fi.id = uf.film_id LEFT JOIN \"mpa_rating\" AS mr" +
                    " ON fi.rating_id = mr.id WHERE fi.id = ? GROUP BY fi.ID";
            Film result = jdbcTemplate.queryForObject(sqlQuery, mapper, id);
            if (result != null) {
                getFilmGenres(result);
            }
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    private void getFilmGenres(Film film) {
        String sqlQuery = "SELECT * FROM \"film_genre\" AS fg LEFT JOIN \"genre\" AS gn ON fg.genre_id = gn.id" +
                " WHERE fg.film_id = ? GROUP BY gn.name ORDER BY gn.id";
        List<Map<String, Object>> genresList = jdbcTemplate.queryForList(sqlQuery, film.getId());
        List<Genre> filmGenres = new ArrayList<>();
        if (!genresList.isEmpty()) {
            for (Map<String, Object> genre : genresList) {
                Genre genre1 = Genre.builder()
                        .id(Long.parseLong(genre.get("id").toString()))
                        .name(genre.get("name").toString())
                        .build();
                filmGenres.add(genre1);
            }
        }
        film.setGenres(filmGenres);
    }

    @Override
    public Film create(Film film) {
        String sqlQuery = "INSERT INTO \"film\" (name, description, release_date, duration, rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setLong(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        if (film.getGenres() != null) {
            String queryForGenres = "INSERT INTO \"film_genre\" (film_id, genre_id) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(queryForGenres, film.getGenres(), film.getGenres().size(),
                    (PreparedStatement ps, Genre genre) -> {
                        ps.setLong(1, film.getId());
                        ps.setLong(2, genre.getId());
                    });
        }
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        String sqlQuery = "UPDATE \"film\" SET " +
                "name = ?, description = ?, release_date = ?, duration = ? " +
                "where id = ?";

        jdbcTemplate.update(sqlQuery,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getId());

        String sqlDeleteGenreQuery = "DELETE FROM \"film_genre\" WHERE film_id = ?";
        jdbcTemplate.update(sqlDeleteGenreQuery, newFilm.getId());

        if (newFilm.getGenres() != null) {
            String queryForGenres = "INSERT INTO \"film_genre\" (film_id, genre_id) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(queryForGenres, newFilm.getGenres(), newFilm.getGenres().size(),
                    (PreparedStatement ps, Genre genre) -> {
                        ps.setLong(1, newFilm.getId());
                        ps.setLong(2, genre.getId());
                    });
        }

        return newFilm;
    }

    @Override
    public void addLike(Film film, User user) {
        String checkQuery = "SELECT count(film_id) FROM \"user_films\" WHERE user_id = ? AND film_id = ?";
        Integer result = jdbcTemplate.queryForObject(checkQuery, Integer.class, user.getId(), film.getId());
        if (result == null || result == 0) {
            String sqlQuery = "INSERT INTO \"user_films\" (user_id, film_id) VALUES  (?, ?)";

            jdbcTemplate.update(sqlQuery,
                    user.getId(),
                    film.getId());
        }
    }

    @Override
    public boolean removeLike(Film film, User user) {
        String sqlQuery = "DELETE FROM \"user_films\" WHERE user_id = ? AND film_id = ?";
        return jdbcTemplate.update(sqlQuery, user.getId(), film.getId()) > 0;
    }

    public boolean delete(long id) {
        String sqlQuery = "DELETE FROM \"film\" WHERE id = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }
}
