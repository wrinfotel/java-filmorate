package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@SpringBootTest
public class UsersControllerTests {

    @Autowired
    private UserController controller;

    @Test
    void shouldCreateUser() {
        User user = User.builder()
                .name("Test")
                .email("test@test.ru")
                .login("testLogin")
                .birthday(LocalDate.parse("2011-05-12"))
                .build();

        User createdUser = controller.create(user);
        Assertions.assertEquals(user, createdUser);
    }

    @Test
    void shouldNotCreateUserWithSameEmail() {
        User user = User.builder()
                .name("Test")
                .email("testEmail@test.ru")
                .login("testLogin")
                .birthday(LocalDate.parse("2011-05-12"))
                .build();
        controller.create(user);
        Assertions.assertThrows(ValidationException.class,
                () -> controller.create(user),
                "Этот имейл уже используется - тест провален");
    }

    @Test
    void shouldNotCreateUserWithoutEmail() {
        User user = User.builder()
                .name("Test")
                .login("testLogin")
                .birthday(LocalDate.parse("2011-05-12"))
                .build();
        Assertions.assertThrows(ValidationException.class,
                () -> controller.create(user),
                "Имейл должен быть указан - тест провален");
    }

    @Test
    void shouldNotCreateUserWithInvalidEmail() {
        User user = User.builder()
                .name("Test")
                .email("testtest.ru")
                .login("testLogin")
                .birthday(LocalDate.parse("2011-05-12"))
                .build();
        Assertions.assertThrows(ValidationException.class,
                () -> controller.create(user),
                "Имейл должен быть указан - тест провален");
    }

    @Test
    void shouldNotCreateUserWithoutLogin() {
        User user = User.builder()
                .name("Test")
                .email("test@test.ru")
                .birthday(LocalDate.parse("2011-05-12"))
                .build();
        Assertions.assertThrows(ValidationException.class,
                () -> controller.create(user),
                "Логин должен быть указан - тест провален");
    }

    @Test
    void shouldNotCreateUserWithSpacesInLogin() {
        User user = User.builder()
                .name("Test")
                .email("test@test.ru")
                .login("tes tLo gin")
                .birthday(LocalDate.parse("2011-05-12"))
                .build();
        Assertions.assertThrows(ValidationException.class,
                () -> controller.create(user),
                "Логин не должен содержать пробелы - тест провален");
    }

    @Test
    void shouldNotCreateUserBitrhdayInFuture() {
        User user = User.builder()
                .name("Test")
                .email("test@test.ru")
                .login("testLogin")
                .birthday(LocalDate.parse("2025-05-12"))
                .build();
        Assertions.assertThrows(ValidationException.class,
                () -> controller.create(user),
                "Дата рождения не может быть в будущем - тест провален");
    }

    @Test
    void shouldUpdateUser() {
        User user = User.builder()
                .name("Test")
                .email("test@test.ru")
                .login("testLogin")
                .birthday(LocalDate.parse("2011-05-12"))
                .build();

        User createdUser = controller.create(user);
        Assertions.assertEquals(user, createdUser);
        Assertions.assertEquals(1, createdUser.getId());

        User userUpdate = User.builder()
                .id(1L)
                .name("TestUpdated")
                .email("test@testupdate.ru")
                .login("testLoginUpdate")
                .birthday(LocalDate.parse("2010-05-12"))
                .build();

        User updateUser = controller.update(userUpdate);
        Assertions.assertEquals(userUpdate, updateUser);
        Assertions.assertEquals(1, updateUser.getId());
    }

    @Test
    void shouldNotUpdateUser() {
        User user = User.builder()
                .id(10L)
                .name("Test")
                .email("test@test.ru")
                .login("testLogin")
                .birthday(LocalDate.parse("2011-05-12"))
                .build();

        Assertions.assertThrows(NotFoundException.class,
                () -> controller.update(user),
                "Пользователь не найден - тест провален");
    }
}
