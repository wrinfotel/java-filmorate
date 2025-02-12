package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {
    public static User updateUserFields(User oldUser, User newUser) {
        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()) {
            oldUser.setEmail(newUser.getEmail());
        }
        if (newUser.getLogin() != null && !newUser.getLogin().isBlank()) {
            oldUser.setLogin(newUser.getLogin());
        }
        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            oldUser.setName(newUser.getName());
        } else {
            oldUser.setName(newUser.getLogin());
        }
        if (newUser.getBirthday() != null) {
            oldUser.setBirthday(newUser.getBirthday());
        }
        return oldUser;
    }

    public static UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setLogin(user.getLogin());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setBirthday(user.getBirthday());
        return dto;
    }
}
