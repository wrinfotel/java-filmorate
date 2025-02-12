package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UsersControllerTests {

    @Autowired
    private UserController controller;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateUser() {
        User user = User.builder()
                .name("Test")
                .email("test" + Math.random() + "@test.ru")
                .login("testLogin")
                .birthday(LocalDate.parse("2011-05-12"))
                .build();

        UserDto createdUser = controller.create(user);
        Assertions.assertNotNull(createdUser.getId());
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
    void shouldNotCreateUserWithoutEmail() throws Exception {

        String json = "{\"name\": \"Test\"," +
                "\"login\": \"testLogin\"," +
                " \"birthday\": \"2011-05-12\"}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(
                        result -> assertInstanceOf(MethodArgumentNotValidException.class,
                                result.getResolvedException()));
    }

    @Test
    void shouldNotCreateUserWithInvalidEmail() throws Exception {
        String json = "{\"name\": \"Test\"," +
                " \"email\": \"testtest.ru\"," +
                "\"login\": \"testLogin\"," +
                " \"birthday\": \"2011-05-12\"}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(
                        result -> assertInstanceOf(MethodArgumentNotValidException.class,
                                result.getResolvedException()));
    }

    @Test
    void shouldNotCreateUserWithoutLogin() throws Exception {
        String json = "{\"name\": \"Test\"," +
                " \"email\": \"testUWL@test.ru\"," +
                " \"birthday\": \"2011-05-12\"}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(
                        result -> assertInstanceOf(MethodArgumentNotValidException.class,
                                result.getResolvedException()));
    }

    @Test
    void shouldNotCreateUserWithSpacesInLogin() throws Exception {
        String json = "{\"name\": \"Test\"," +
                " \"email\": \"testWs@test.ru\"," +
                " \"login\": \"tes tLog in\"," +
                " \"birthday\": \"2011-05-12\"}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(
                        result -> assertInstanceOf(MethodArgumentNotValidException.class,
                                result.getResolvedException()));
    }

    @Test
    void shouldNotCreateUserBitrhdayInFuture() throws Exception {
        String json = "{\"name\": \"Test\"," +
                " \"email\": \"testUWL@test.ru\"," +
                " \"login\": \"testLogin\"," +
                " \"birthday\": \"2025-05-12\"}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(
                        result -> assertInstanceOf(MethodArgumentNotValidException.class,
                                result.getResolvedException()));
    }

    @Test
    void shouldUpdateUser() {
        User user = User.builder()
                .name("Test")
                .email("testUuU" + Math.random() + "@test.ru")
                .login("testLogin")
                .birthday(LocalDate.parse("2011-05-12"))
                .build();

        UserDto createdUser = controller.create(user);
        Assertions.assertNotNull(createdUser.getId());

        User userUpdate = User.builder()
                .id(createdUser.getId())
                .name("TestUpdated")
                .email("test@testupdate.ru")
                .login("testLoginUpdate")
                .birthday(LocalDate.parse("2010-05-12"))
                .build();

        UserDto updateUser = controller.update(userUpdate);
        Assertions.assertEquals(userUpdate.getName(), updateUser.getName());
        Assertions.assertEquals(userUpdate.getEmail(), updateUser.getEmail());
        Assertions.assertEquals(userUpdate.getLogin(), updateUser.getLogin());
        Assertions.assertEquals(createdUser.getId(), updateUser.getId());
    }

    @Test
    void shouldNotUpdateUser() {
        User user = User.builder()
                .id(10000L)
                .name("Test")
                .email("testNuu@test.ru")
                .login("testLogin")
                .birthday(LocalDate.parse("2011-05-12"))
                .build();

        Assertions.assertThrows(NotFoundException.class,
                () -> controller.update(user),
                "Пользователь не найден - тест провален");
    }
}
