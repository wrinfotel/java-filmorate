package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<UserDto> findAll() {
        return userStorage.findAll().stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public User findById(long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }

    public UserDto create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        boolean checkUser = findAll().stream().anyMatch(user1 -> user1.getEmail().equals(user.getEmail()));
        if (checkUser) {
            throw new ValidationException("Этот имейл уже используется");
        }
        User createdUser = userStorage.create(user);
        log.info("Created new user with id " + user.getId());
        return UserMapper.mapToUserDto(createdUser);
    }

    public UserDto update(User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        User oldUser = userStorage.findById(newUser.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден"));
        User updatedUser = userStorage.update(UserMapper.updateUserFields(oldUser, newUser));
        log.info("Updated user with id " + updatedUser.getId());
        return UserMapper.mapToUserDto(updatedUser);
    }

    public void addFriend(long userId, long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);
        List<UserDto> userFriends = userFriends(userId);
        boolean checkFriendship = userFriends.stream().anyMatch(user1 -> user1.getId() == friendId);
        if (!checkFriendship) {
            boolean checkResult = checkAndAcceptFriendship(friend, user);
            userStorage.addFriend(user, friend, checkResult);

        }
    }

    private boolean checkAndAcceptFriendship(User friend, User user) {
        List<UserDto> userFriends = userFriends(friend.getId());
        boolean checkFriendship = userFriends.stream().anyMatch(user1 -> Objects.equals(user1.getId(), user.getId()));
        if (checkFriendship) {
            changeFriendshipStatus(friend, user, true);
        }
        return checkFriendship;
    }

    private void changeFriendshipStatus(User user, User friend, boolean status) {
        userStorage.acceptFriendship(user, friend, status);
    }

    public void deleteFriend(long userId, long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);
        userStorage.removeFriend(user, friend);
        changeFriendshipStatus(friend, user, false);
    }

    public List<UserDto> commonFriends(long userId, long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);
        List<User> userFriends = userStorage.getFriends(user);
        List<User> friendList = userStorage.getFriends(friend);
        return userFriends.stream().filter(friendList::contains).map(UserMapper::mapToUserDto).toList();
    }

    public List<UserDto> userFriends(long userId) {
        User user = findById(userId);
        return userStorage.getFriends(user).stream().map(UserMapper::mapToUserDto).toList();
    }
}
