package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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

    @NotNull
    private LocalDate releaseDate;

    @Positive
    private int duration;

    private int likesCount;

    private MpaRating mpa;

    private List<Genre> genres;

    private List<Director> directors;

    public void addLike() {
        this.likesCount++;
    }

    public void removeLike() {
        this.likesCount--;
    }
}
