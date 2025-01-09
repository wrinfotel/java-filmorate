package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class User {
    Long id;

    @Email
    String email;

    String login;
    String name;
    LocalDate birthday;
}
