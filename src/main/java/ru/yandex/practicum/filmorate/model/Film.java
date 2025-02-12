package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class Film {
    private Long id;

    @NotNull
    @NotBlank
    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    @Past
    @NotNull
    private LocalDate releaseDate;

    @Positive
    private int duration;

    private int likesCount;

    private MpaRating mpa;

    private List<Genre> genres;

    public void addLike() {
        this.likesCount++;
    }

    public void removeLike() {
        this.likesCount--;
    }
}
