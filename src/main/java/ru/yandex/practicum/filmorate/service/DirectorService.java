package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final Logger log = LoggerFactory.getLogger(DirectorService.class);

    private final DirectorStorage directorStorage;

    public Collection<Director> findAll() {
        return directorStorage.findAll();
    }

    public Director findById(Long id) {
        return directorStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Режиссер с id = " + id + " не найден"));
    }

    public Director create(Director director) {
        Director createdDirector = directorStorage.create(director);
        log.info("Created new director with id " + createdDirector.getId());
        return createdDirector;
    }

    public Director update(Director newDirector) {
        if (newDirector.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        Director oldDirector = directorStorage.findById(newDirector.getId())
                .orElseThrow(() -> new NotFoundException("Режиссер с id = " + newDirector.getId() + " не найден"));
        Director updatedDirector = directorStorage.update(DirectorMapper
                .updateDirectorFields(oldDirector, newDirector));
        log.info("Updated director with id " + updatedDirector.getId());
        return updatedDirector;
    }

    public void delete(Long id) {
        Director director = findById(id);
        directorStorage.delete(director);
    }
}
