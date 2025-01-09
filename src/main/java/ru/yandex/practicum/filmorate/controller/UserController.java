package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private final static Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        validateUser(user);
        boolean checkUser = users.values().stream().anyMatch(user1 -> user1.getEmail().equals(user.getEmail()));
        if (checkUser) {
            throw new ValidationException("Этот имейл уже используется");
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Created new user with id " + user.getId());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            validateUser(newUser);
            if (!newUser.getEmail().isBlank()) {
                oldUser.setEmail(newUser.getEmail());
            }
            if (!newUser.getLogin().isBlank()) {
                oldUser.setLogin(newUser.getLogin());
            }
            if (!newUser.getName().isBlank()) {
                oldUser.setName(newUser.getName());
            }

            if (newUser.getBirthday() != null) {
                oldUser.setBirthday(newUser.getBirthday());
            }
            log.info("Updated user with id " + oldUser.getId());
            return oldUser;
        }
        log.error("Пользователь с id = " + newUser.getId() + " не найден");
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Ошибка валидации - Имейл должен быть указан");
            throw new ValidationException("Имейл должен быть указан");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Ошибка валидации - Логин должен быть указан");
            throw new ValidationException("Логин должен быть указан");
        }
        if (user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации - Логин не должен содержать пробелы");
            throw new ValidationException("Логин не должен содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации - Дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
