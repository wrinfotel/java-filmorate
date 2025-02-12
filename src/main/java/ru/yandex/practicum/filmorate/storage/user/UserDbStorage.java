package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> mapper;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = new UserRowMapper();
    }

    @Override
    public Collection<User> findAll() {
        String sqlQuery = "SELECT * FROM \"user\"";
        return jdbcTemplate.query(sqlQuery, mapper);
    }

    @Override
    public Optional<User> findOne(long id) {
        try {
            String sqlQuery = "SELECT * FROM \"user\" WHERE id = ?";
            User result = jdbcTemplate.queryForObject(sqlQuery, mapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public void addFriend(User user, User friend, boolean status) {
        String sqlQuery = "INSERT INTO \"user_friends\" (user_id, friend_id, status) " +
                "values (?, ?, ?)";
        jdbcTemplate.update(sqlQuery,
                user.getId(),
                friend.getId(),
                status);
    }

    @Override
    public void removeFriend(User user, User friend) {
        String sqlQuery = "DELETE FROM \"user_friends\" WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sqlQuery, user.getId(), friend.getId());
    }

    @Override
    public List<User> getFriends(User user) {
        String sqlQuery = "SELECT * FROM \"user\" WHERE id IN (" +
                "SELECT friend_id FROM \"user_friends\" WHERE user_id = ?)";
        return jdbcTemplate.query(sqlQuery, mapper, user.getId());
    }

    @Override
    public void acceptFriendship(User user, User friend, boolean status) {
        String sqlQuery = "UPDATE \"user_friends\" SET " +
                "status = ? " +
                "WHERE user_id = ? AND friend_id = ?";

        jdbcTemplate.update(sqlQuery,
                status,
                user.getId(),
                friend.getId());

    }

    @Override
    public User create(User user) {
        String sqlQuery = "INSERT INTO \"user\" (email, login, name, birthday) " +
                "VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public User update(User newUser) {
        String sqlQuery = "UPDATE \"user\" SET " +
                "email = ?, login = ?, name = ?, birthday = ? " +
                "where id = ?";

        jdbcTemplate.update(sqlQuery,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                newUser.getBirthday(), newUser.getId());

        return newUser;
    }

    public boolean delete(long id) {
        String sqlQuery = "DELETE FROM \"user\" WHERE id = ?";

        return jdbcTemplate.update(sqlQuery, id) > 0;
    }
}
