package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserStorage userStorage;

    public Collection<User> findAll() {
        return userStorage.findAll().values();
    }

    public User findById(long userId) {
        if (!userStorage.findAll().containsKey(userId)) {
            log.error("Пользователь с id = " + userId + " не найден");
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        return userStorage.findAll().get(userId);
    }

    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        boolean checkUser = findAll().stream().anyMatch(user1 -> user1.getEmail().equals(user.getEmail()));
        if (checkUser) {
            throw new ValidationException("Этот имейл уже используется");
        }
        User createdUser = userStorage.create(user);
        log.info("Created new user with id " + user.getId());
        return createdUser;
    }

    public User update(User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!userStorage.findAll().containsKey(newUser.getId())) {
            log.error("Пользователь с id = " + newUser.getId() + " не найден");
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        User updatedUser = userStorage.update(newUser);
        log.info("Updated user with id " + updatedUser.getId());
        return updatedUser;
    }

    public void addFriend(long userId, long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);
        user.getFriends().add(friend.getId());
        friend.getFriends().add(user.getId());
    }

    public void deleteFriend(long userId, long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);
        user.getFriends().remove(friend.getId());
        friend.getFriends().remove(user.getId());
    }

    public List<User> commonFriends(long userId, long friendId) {
        Set<Long> userFriends = findById(userId).getFriends();
        Set<Long> friendList = findById(friendId).getFriends();
        List<Long> commonFriends = userFriends.stream().filter(friendList::contains).toList();
        List<User> commonList = new ArrayList<>();
        if (!commonFriends.isEmpty()) {
            for (Long commonFriend : commonFriends) {
                commonList.add(findById(commonFriend));
            }
        }
        return commonList;
    }

    public List<User> userFriends(long userId) {
        Set<Long> userFriends = findById(userId).getFriends();

        List<User> friendsList = new ArrayList<>();
        if (!userFriends.isEmpty()) {
            for (Long friendId : userFriends) {
                friendsList.add(findById(friendId));
            }
        }
        return friendsList;
    }
}
