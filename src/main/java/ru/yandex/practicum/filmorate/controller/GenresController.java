package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenresController {

    private final GenreService genresService;

    @GetMapping
    public Collection<Genre> findAll() {
        return genresService.findAll();
    }

    @GetMapping("/{ratingId}")
    public Genre getFilmById(@PathVariable Long ratingId) {
        return genresService.findById(ratingId);
    }
}
