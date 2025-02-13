package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        user.setId(getNextId());
        user.setFriends(new HashSet<>());
        user.setLikedFilms(new HashSet<>());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User newUser) {
        User oldUser = users.get(newUser.getId());
        if (!newUser.getEmail().isBlank()) {
            oldUser.setEmail(newUser.getEmail());
        }
        if (!newUser.getLogin().isBlank()) {
            oldUser.setLogin(newUser.getLogin());
        }
        if (!newUser.getName().isBlank()) {
            oldUser.setName(newUser.getName());
        } else {
            oldUser.setName(newUser.getLogin());
        }
        if (newUser.getBirthday() != null) {
            oldUser.setBirthday(newUser.getBirthday());
        }
        return oldUser;
    }

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void addFriend(User user, User friend, boolean status) {
        user.getFriends().add(friend);
    }

    @Override
    public void removeFriend(User user, User friend) {
        user.getFriends().remove(friend);
    }

    @Override
    public List<User> getFriends(User user) {
        return user.getFriends().stream().toList();
    }

    @Override
    public void acceptFriendship(User friend, User user, boolean status) {

    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
