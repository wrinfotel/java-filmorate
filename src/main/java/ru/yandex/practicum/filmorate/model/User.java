package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class User {
    private Long id;

    @NotNull
    @NotBlank
    @Email
    private String email;

    @NotNull
    @NotBlank
    @Pattern(regexp = "\\S+", message = "Поле не должно содержать пробелов")
    private String login;

    private String name;

    @Past
    private LocalDate birthday;

    private Set<Long> friends;

    private Set<Long> likedFilms;
}
