package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class User {
    Long id;

    @NotNull
    @NotBlank
    @Email
    String email;

    @NotNull
    @NotBlank
    @Pattern(regexp = "\\S+", message = "Поле не должно содержать пробелов")
    String login;

    String name;

    @Past
    LocalDate birthday;

    Set<Long> friends;

    Set<Long> likedFilms;
}
