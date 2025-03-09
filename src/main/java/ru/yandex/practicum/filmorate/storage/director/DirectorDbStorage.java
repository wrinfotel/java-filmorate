package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.mapper.DirectorRowMapper;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Repository
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Director> mapper;

    @Autowired
    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = new DirectorRowMapper();
    }

    @Override
    public Collection<Director> findAll() {
        String sqlQuery = "SELECT * FROM \"director\"";
        return jdbcTemplate.query(sqlQuery, mapper);
    }

    @Override
    public Optional<Director> findById(long id) {
        try {
            String sqlQuery = "SELECT * FROM \"director\" WHERE id = ?";
            Director result = jdbcTemplate.queryForObject(sqlQuery, mapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public Director create(Director director) {
        String sqlQuery = "INSERT INTO \"director\" (name) " +
                "VALUES (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);

        director.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return director;
    }

    @Override
    public Director update(Director newDirector) {
        String sqlQuery = "UPDATE \"director\" SET " +
                "name = ? " +
                "where id = ?";

        jdbcTemplate.update(sqlQuery,
                newDirector.getName(),
                newDirector.getId());

        return newDirector;
    }

    @Override
    public boolean delete(Director director) {
        String sqlQuery = "DELETE FROM \"director\" WHERE id = ?";
        return jdbcTemplate.update(sqlQuery, director.getId()) > 0;
    }
}
