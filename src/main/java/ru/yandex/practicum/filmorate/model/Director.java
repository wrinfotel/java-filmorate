package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Objects;

@Data
@Builder
public class Director {
    private Long id;

    @NotNull
    @NotBlank
    private String name;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Director director = (Director) object;
        return Objects.equals(id, director.id);
    }
}
