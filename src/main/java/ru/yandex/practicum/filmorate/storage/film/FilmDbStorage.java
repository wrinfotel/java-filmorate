package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.FilmListRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@Repository("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Film> mapper;
    private final RowMapper<List<Film>> listMapper;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.listMapper = new FilmListRowMapper();
        this.mapper = new FilmRowMapper();
    }

    @Override
    public Collection<Film> findAll() {
        String sqlQuery = "SELECT fi.*, (SELECT COUNT(film_id) FROM \"user_films\" WHERE film_id = fi.id)" +
                " AS likes_count, mpa.name AS mpa_name, mpa.id AS mpa_id, gen.name AS genre_name, gen.id AS genre_id," +
                " dir.id AS director_id, dir.name AS director_name" +
                " FROM \"film\" AS fi LEFT JOIN \"film_genre\" AS fg ON fi.ID = fg.FILM_ID" +
                " LEFT JOIN \"genre\" AS gen ON fg.GENRE_ID = gen.ID" +
                " LEFT JOIN \"film_director\" AS fd ON fi.ID = fd.FILM_ID" +
                " LEFT JOIN \"director\" AS dir ON fd.DIRECTOR_ID = dir.ID" +
                " LEFT JOIN \"mpa_rating\" AS mpa ON fi.RATING_ID = mpa.id";
        try {
            return jdbcTemplate.query(sqlQuery, listMapper).getFirst();
        } catch (NoSuchElementException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Произошла ошибка при билде", e);
        }

    }

    @Override
    public Optional<Film> findById(long id) {
        try {
            String sqlQuery = "SELECT fi.*, (SELECT COUNT(film_id) FROM \"user_films\" WHERE film_id = fi.id)" +
                    " AS likes_count, mpa.name AS mpa_name, mpa.id AS mpa_id, gen.name AS genre_name, gen.id AS genre_id," +
                    " dir.id AS director_id, dir.name AS director_name" +
                    " FROM \"film\" AS fi LEFT JOIN \"film_genre\" AS fg ON fi.ID = fg.FILM_ID" +
                    " LEFT JOIN \"genre\" AS gen ON fg.GENRE_ID = gen.ID" +
                    " LEFT JOIN \"film_director\" AS fd ON fi.ID = fd.FILM_ID" +
                    " LEFT JOIN \"director\" AS dir ON fd.DIRECTOR_ID = dir.ID" +
                    " LEFT JOIN \"mpa_rating\" AS mpa ON fi.RATING_ID = mpa.id" +
                    " WHERE fi.id = ?";
            Film result = jdbcTemplate.queryForObject(sqlQuery, mapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
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
            List<Genre> withoutDublicates = film.getGenres().stream().distinct().toList();
            film.setGenres(withoutDublicates);
            String queryForGenres = "INSERT INTO \"film_genre\" (film_id, genre_id) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(queryForGenres, film.getGenres(), film.getGenres().size(),
                    (PreparedStatement ps, Genre genre) -> {
                        ps.setLong(1, film.getId());
                        ps.setLong(2, genre.getId());
                    });
        }

        if (film.getDirectors() != null) {
            List<Director> directors = film.getDirectors().stream().distinct().toList();
            film.setDirectors(directors);
            String queryForDirectors = "INSERT INTO \"film_director\" (film_id, director_id) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(queryForDirectors, film.getDirectors(), film.getDirectors().size(),
                    (PreparedStatement ps, Director director) -> {
                        ps.setLong(1, film.getId());
                        ps.setLong(2, director.getId());
                    });
        }

        return film;
    }

    @Override
    public Film update(Film newFilm) {
        String sqlQuery = "UPDATE \"film\" SET " +
                "name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? " +
                "where id = ?";

        jdbcTemplate.update(sqlQuery,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getMpa().getId(),
                newFilm.getId());

        String sqlDeleteGenreQuery = "DELETE FROM \"film_genre\" WHERE film_id = ?";
        jdbcTemplate.update(sqlDeleteGenreQuery, newFilm.getId());

        if (newFilm.getGenres() != null) {
            List<Genre> genres = newFilm.getGenres().stream().distinct().toList();
            newFilm.setGenres(genres);
            String queryForGenres = "INSERT INTO \"film_genre\" (film_id, genre_id) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(queryForGenres, newFilm.getGenres(), newFilm.getGenres().size(),
                    (PreparedStatement ps, Genre genre) -> {
                        ps.setLong(1, newFilm.getId());
                        ps.setLong(2, genre.getId());
                    });
        }

        String sqlDeleteGenreQueryDirector = "DELETE FROM \"film_director\" WHERE film_id = ?";
        jdbcTemplate.update(sqlDeleteGenreQueryDirector, newFilm.getId());

        if (newFilm.getDirectors() != null) {
            List<Director> directors = newFilm.getDirectors().stream().distinct().toList();
            newFilm.setDirectors(directors);
            String queryForDirectors = "INSERT INTO \"film_director\" (film_id, director_id) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(queryForDirectors, newFilm.getDirectors(), newFilm.getDirectors().size(),
                    (PreparedStatement ps, Director director) -> {
                        ps.setLong(1, newFilm.getId());
                        ps.setLong(2, director.getId());
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

    @Override
    public Collection<Film> findFilmsByDirector(Director director, String sortField) {
        String orderBy = sortField.equals("year") ? "YEAR(fi.release_date) ASC" : "likes_count DESC";
        String sqlQuery = "SELECT fi.*, (SELECT COUNT(film_id) FROM \"user_films\" WHERE film_id = fi.id)" +
                " AS likes_count, mpa.name AS mpa_name, mpa.id AS mpa_id, gen.name AS genre_name, gen.id AS genre_id," +
                " dir.id AS director_id, dir.name AS director_name" +
                " FROM \"film\" AS fi LEFT JOIN \"film_genre\" AS fg ON fi.ID = fg.FILM_ID" +
                " LEFT JOIN \"genre\" AS gen ON fg.GENRE_ID = gen.ID" +
                " LEFT JOIN \"film_director\" AS fd ON fi.ID = fd.FILM_ID" +
                " LEFT JOIN \"director\" AS dir ON fd.DIRECTOR_ID = dir.ID" +
                " LEFT JOIN \"mpa_rating\" AS mpa ON fi.RATING_ID = mpa.id" +
                " WHERE director_id = ? ORDER BY " + orderBy;

        return jdbcTemplate.query(sqlQuery, listMapper, director.getId()).getFirst();
    }

    @Override
    public boolean deleteById(long id) {
        String sqlQuery = "DELETE FROM \"film\" WHERE id = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }

    @Override
    public Collection<Film> getPopularFilm(Integer genreId, Integer year) {
        String sqlQuery;
        if (genreId == 0 && year == 0) {
            sqlQuery = "SELECT fi.*, (SELECT COUNT(film_id) FROM \"user_films\" WHERE film_id = fi.id)" +
                    " AS likes_count, mpa.name AS mpa_name, mpa.id AS mpa_id, gen.name AS genre_name, gen.id AS genre_id," +
                    " dir.id AS director_id, dir.name AS director_name" +
                    " FROM \"film\" AS fi LEFT JOIN \"film_genre\" AS fg ON fi.ID = fg.FILM_ID" +
                    " LEFT JOIN \"genre\" AS gen ON fg.GENRE_ID = gen.ID" +
                    " LEFT JOIN \"film_director\" AS fd ON fi.ID = fd.FILM_ID" +
                    " LEFT JOIN \"director\" AS dir ON fd.DIRECTOR_ID = dir.ID" +
                    " LEFT JOIN \"mpa_rating\" AS mpa ON fi.RATING_ID = mpa.id" +
                    " ORDER BY likes_count DESC";
        }  else if (genreId != 0 && year == 0) {
            sqlQuery = "SELECT fi.*, (SELECT COUNT(film_id) FROM \"user_films\" WHERE film_id = fi.id)" +
                    " AS likes_count, mpa.name AS mpa_name, mpa.id AS mpa_id, gen.name AS genre_name, gen.id AS genre_id," +
                    " dir.id AS director_id, dir.name AS director_name" +
                    " FROM \"film\" AS fi LEFT JOIN \"film_genre\" AS fg ON fi.ID = fg.FILM_ID" +
                    " LEFT JOIN \"genre\" AS gen ON fg.GENRE_ID = gen.ID" +
                    " LEFT JOIN \"film_director\" AS fd ON fi.ID = fd.FILM_ID" +
                    " LEFT JOIN \"director\" AS dir ON fd.DIRECTOR_ID = dir.ID" +
                    " LEFT JOIN \"mpa_rating\" AS mpa ON fi.RATING_ID = mpa.id" +
                    " WHERE fi.id IN (SELECT f.id FROM \"film\" AS f LEFT JOIN \"film_genre\" AS fgen" +
                    " ON f.id = fgen.film_id WHERE fgen.genre_id = " + genreId + ") " +
                    " ORDER BY likes_count DESC";
        } else if (genreId == 0) {
            sqlQuery = "SELECT fi.*, (SELECT COUNT(film_id) FROM \"user_films\" WHERE film_id = fi.id)" +
                    " AS likes_count, mpa.name AS mpa_name, mpa.id AS mpa_id, gen.name AS genre_name, gen.id AS genre_id," +
                    " dir.id AS director_id, dir.name AS director_name" +
                    " FROM \"film\" AS fi LEFT JOIN \"film_genre\" AS fg ON fi.ID = fg.FILM_ID" +
                    " LEFT JOIN \"genre\" AS gen ON fg.GENRE_ID = gen.ID" +
                    " LEFT JOIN \"film_director\" AS fd ON fi.ID = fd.FILM_ID" +
                    " LEFT JOIN \"director\" AS dir ON fd.DIRECTOR_ID = dir.ID" +
                    " LEFT JOIN \"mpa_rating\" AS mpa ON fi.RATING_ID = mpa.id" +
                    " WHERE EXTRACT (YEAR FROM CAST (fi.release_date AS date)) = " + year + " " +
                    " ORDER BY likes_count DESC";
        } else {
            sqlQuery = "SELECT fi.*, (SELECT COUNT(film_id) FROM \"user_films\" WHERE film_id = fi.id)" +
                    " AS likes_count, mpa.name AS mpa_name, mpa.id AS mpa_id, gen.name AS genre_name, gen.id AS genre_id," +
                    " dir.id AS director_id, dir.name AS director_name" +
                    " FROM \"film\" AS fi LEFT JOIN \"film_genre\" AS fg ON fi.ID = fg.FILM_ID" +
                    " LEFT JOIN \"genre\" AS gen ON fg.GENRE_ID = gen.ID" +
                    " LEFT JOIN \"film_director\" AS fd ON fi.ID = fd.FILM_ID" +
                    " LEFT JOIN \"director\" AS dir ON fd.DIRECTOR_ID = dir.ID" +
                    " LEFT JOIN \"mpa_rating\" AS mpa ON fi.RATING_ID = mpa.id" +
                    " WHERE fi.id IN (SELECT f.id FROM \"film\" AS f LEFT JOIN \"film_genre\" AS fgen" +
                    "  ON f.id = fgen.film_id WHERE fgen.genre_id = " + genreId + ") AND " +
                    "EXTRACT (YEAR FROM CAST (fi.release_date AS date)) = " + year + " ORDER BY likes_count DESC";
        }
        try {
            return jdbcTemplate.query(sqlQuery, listMapper).getFirst();
        } catch (NoSuchElementException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Film> search(String query, String searchBy) {
        String searchQuery = searchQueryBuilder(query, searchBy);
        String sqlQuery = "SELECT fi.*, (SELECT COUNT(film_id) FROM \"user_films\" WHERE film_id = fi.id)" +
                " AS likes_count, mpa.name AS mpa_name, mpa.id AS mpa_id, gen.name AS genre_name, gen.id AS genre_id," +
                " dir.id AS director_id, dir.name AS director_name" +
                " FROM \"film\" AS fi LEFT JOIN \"film_genre\" AS fg ON fi.ID = fg.FILM_ID" +
                " LEFT JOIN \"genre\" AS gen ON fg.GENRE_ID = gen.ID" +
                " LEFT JOIN \"film_director\" AS fd ON fi.ID = fd.FILM_ID" +
                " LEFT JOIN \"director\" AS dir ON fd.DIRECTOR_ID = dir.ID" +
                " LEFT JOIN \"mpa_rating\" AS mpa ON fi.RATING_ID = mpa.id" +
                " WHERE " + searchQuery + " ORDER BY likes_count DESC";
        try {
            return jdbcTemplate.query(sqlQuery, listMapper).getFirst();
        } catch (NoSuchElementException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Произошла ошибка при билде", e);
        }
    }

    private String searchQueryBuilder(String query, String searchBy) {
        String[] searchFields = searchBy.split(",");
        StringBuilder searchQuery = new StringBuilder();
        if (searchFields.length > 0) {
            for (String searchField : searchFields) {
                if (searchField.equals("director")) {
                    if (!searchQuery.isEmpty()) {
                        searchQuery.append(" OR ");
                    }
                    searchQuery.append("LOWER(dir.name) LIKE LOWER('%").append(query).append("%')");
                }
                if (searchField.equals("title")) {
                    if (!searchQuery.isEmpty()) {
                        searchQuery.append(" OR ");
                    }
                    searchQuery.append("LOWER(fi.name " +
                            ") LIKE LOWER('%").append(query).append("%')");
                }
            }
        } else {
            searchQuery.append("LOWER(fi.name " +
                    ") LIKE LOWER('%").append(query).append("%')");
        }
        return searchQuery.toString();
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        String sqlQuery = "SELECT f.*, " +
                "g.id AS genre_id, g.name AS genre_name, " +
                "mpa.NAME AS mpa_name, mpa.ID AS mpa_id, " +
                "dir.id AS director_id, dir.name AS director_name, " +
                "(SELECT COUNT(film_id) FROM \"user_films\" uf WHERE uf.film_id = f.id) AS likes_count " +
                "FROM \"film\" f " +
                "LEFT JOIN \"film_genre\" fg ON f.id = fg.film_id " +
                "LEFT JOIN \"genre\" g ON fg.genre_id = g.id " +
                "LEFT JOIN \"user_films\" uf1 ON f.id = uf1.film_id " +
                "LEFT JOIN \"user_films\" uf2 ON f.id = uf2.film_id " +
                "LEFT JOIN \"mpa_rating\" mpa ON f.rating_id = mpa.id " +
                "LEFT JOIN \"film_director\" AS fd ON f.ID = fd.FILM_ID " +
                "LEFT JOIN \"director\" AS dir ON fd.DIRECTOR_ID = dir.ID " +
                "WHERE uf1.user_id = ? AND uf2.user_id = ? " +
                "ORDER BY likes_count DESC";

        try {
            return jdbcTemplate.query(sqlQuery, listMapper, userId, friendId).getFirst();
        } catch (NoSuchElementException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Произошла ошибка при билде", e);
        }
    }

    @Override
    public List<Film> getRecommendations(Long userId) {
        String sqlQuery = "SELECT f.*, " +
                "(SELECT COUNT(film_id) FROM \"user_films\" uf WHERE uf.film_id = f.id) AS likes_count, " +
                "mpa.NAME AS mpa_name, mpa.ID AS mpa_id, " +
                "g.id AS genre_id, g.name AS genre_name, " +
                "dir.id AS director_id, dir.name AS director_name " +
                "FROM \"film\" f " +
                "JOIN (SELECT uf.film_id, SUM(su.common_likes) " +
                "AS total_similarity FROM \"user_films\" uf " +
                "JOIN (SELECT uf.user_id, COUNT(*) AS common_likes FROM \"user_films\" uf " +
                "JOIN (SELECT film_id FROM \"user_films\" WHERE user_id = ?) ulf ON uf.film_id = ulf.film_id " +
                "WHERE uf.user_id != ? GROUP BY uf.user_id) su ON uf.user_id = su.user_id " +
                "LEFT JOIN (SELECT film_id FROM \"user_films\" WHERE user_id = ?) ulf " +
                "ON uf.film_id = ulf.film_id GROUP BY uf.film_id) rf ON f.id = rf.film_id " +
                "LEFT JOIN \"mpa_rating\" mpa ON f.rating_id = mpa.id " +
                "LEFT JOIN \"film_genre\" fg ON f.id = fg.film_id " +
                "LEFT JOIN \"genre\" g ON fg.genre_id = g.id " +
                "LEFT JOIN \"film_director\" AS fd ON f.ID = fd.FILM_ID " +
                "LEFT JOIN \"director\" AS dir ON fd.DIRECTOR_ID = dir.ID " +
                "WHERE f.id NOT IN (SELECT film_id FROM \"user_films\" WHERE user_id = ?) " +
                "ORDER BY rf.total_similarity DESC";

        try {
            return jdbcTemplate.query(sqlQuery, listMapper, userId, userId, userId, userId).getFirst();
        } catch (NoSuchElementException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Произошла ошибка при билде", e);
        }
    }
}