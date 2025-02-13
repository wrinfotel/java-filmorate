package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<MpaRating> mapper;

    @Autowired
    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = new MpaRowMapper();
    }

    @Override
    public Collection<MpaRating> findAll() {
        String sqlQuery = "SELECT * FROM \"mpa_rating\"";
        return jdbcTemplate.query(sqlQuery, mapper);
    }

    @Override
    public Optional<MpaRating> findById(long id) {
        try {
            String sqlQuery = "SELECT * FROM \"mpa_rating\" WHERE id = ?";
            MpaRating result = jdbcTemplate.queryForObject(sqlQuery, mapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}
