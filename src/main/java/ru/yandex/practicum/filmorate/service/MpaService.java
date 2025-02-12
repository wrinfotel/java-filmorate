package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;

@Service
public class MpaService {

    @Autowired
    private MpaStorage mpaStorage;

    public Collection<MpaRating> findAll() {
        return mpaStorage.findAll();
    }

    public MpaRating findById(Long id) {
        return mpaStorage.findOne(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id = " + id + " не найден"));
    }
}
