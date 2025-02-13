package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Genre> mapper;

    @Autowired
    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = new GenreRowMapper();
    }

    @Override
    public Collection<Genre> findAll() {
        String sqlQuery = "SELECT * FROM \"genre\"";
        return jdbcTemplate.query(sqlQuery, mapper);
    }

    @Override
    public Optional<Genre> findById(long id) {
        try {
            String sqlQuery = "SELECT * FROM \"genre\" WHERE id = ?";
            Genre result = jdbcTemplate.queryForObject(sqlQuery, mapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}
