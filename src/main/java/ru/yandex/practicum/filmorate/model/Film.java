package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class Film {
    Long id;

    @NotNull
    @NotBlank
    String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    String description;

    @Past
    LocalDate releaseDate;

    @Positive
    int duration;

    int likesCount;

    public void addLike() {
        this.likesCount++;
    }

    public void removeLike() {
        this.likesCount--;
    }
}
