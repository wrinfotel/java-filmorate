package ru.yandex.practicum.filmorate.dbTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class})
class UserDbStorageTests {

    private final UserDbStorage userStorage;

    private final User newUser = User.builder()
            .name("Test")
            .email("test@test.ru")
            .login("testLogin")
            .birthday(LocalDate.parse("2011-05-12"))
            .build();

    private final User newUser2 = User.builder()
            .name("Test2")
            .email("test2@test.ru")
            .login("test2Login")
            .birthday(LocalDate.parse("2007-05-20"))
            .build();

    @Test
    public void testCreateUser() {
        User userOptional = userStorage.create(newUser);
        Assertions.assertNotNull(userOptional.getId());
        Collection<User> users = userStorage.findAll();
        Assertions.assertEquals(1, users.size());
    }


    @Test
    public void testFindUserById() {
        User created = userStorage.create(newUser);
        Optional<User> userOptional = userStorage.findOne(created.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", created.getId())
                );
    }

    @Test
    public void testGetAllUsers() {
        userStorage.create(newUser);
        userStorage.create(newUser2);

        Collection<User> users = userStorage.findAll();
        Assertions.assertEquals(2, users.size());
    }

    @Test
    public void testUpdateUser() {
        User userCreated = userStorage.create(newUser);
        userCreated.setName("TestUpdated");
        userStorage.update(userCreated);
        Optional<User> updated = userStorage.findOne(userCreated.getId());

        assertThat(updated)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("name", "TestUpdated")
                );
    }

    @Test
    public void testAddFriendUser() {
        User friend1 = userStorage.create(newUser);
        User friend2 = userStorage.create(newUser2);

        userStorage.addFriend(friend1, friend2, false);
        List<User> friend1Friends = userStorage.getFriends(friend1);
        Assertions.assertEquals(1, friend1Friends.size());
    }

    @Test
    public void testRemoveFriendUser() {
        User friend1 = userStorage.create(newUser);
        User friend2 = userStorage.create(newUser2);

        userStorage.addFriend(friend1, friend2, false);
        List<User> friend1Friends = userStorage.getFriends(friend1);
        Assertions.assertEquals(1, friend1Friends.size());

        userStorage.removeFriend(friend1, friend2);
        List<User> friend1RemovedFriends = userStorage.getFriends(friend1);
        Assertions.assertEquals(0, friend1RemovedFriends.size());
    }

    @Test
    public void testDeleteUsers() {
        userStorage.create(newUser);
        User toBeDeleted = userStorage.create(newUser2);

        Collection<User> users = userStorage.findAll();
        Assertions.assertEquals(2, users.size());

        userStorage.delete(toBeDeleted.getId());

        Collection<User> usersAfterDelete = userStorage.findAll();
        Assertions.assertEquals(1, usersAfterDelete.size());

    }
}