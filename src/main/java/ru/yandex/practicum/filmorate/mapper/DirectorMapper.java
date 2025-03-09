package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.Director;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DirectorMapper {

    public static Director updateDirectorFields(Director oldDirector, Director newDirector) {
        if (newDirector.getName() != null && !newDirector.getName().isBlank()) {
            oldDirector.setName(newDirector.getName());
        }
        return oldDirector;
    }
}
