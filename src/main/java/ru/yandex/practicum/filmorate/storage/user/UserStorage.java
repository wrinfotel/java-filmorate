package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {

    Collection<User> findAll();

    User create(User user);

    User update(User newUser);

    Optional<User> findOne(long id);

    void addFriend(User user, User friend, boolean status);

    void removeFriend(User user, User friend);

    List<User> getFriends(User user);

    void acceptFriendship(User user, User friend, boolean status);
}
