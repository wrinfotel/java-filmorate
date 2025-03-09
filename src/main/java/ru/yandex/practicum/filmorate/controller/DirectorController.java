package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<Director> findAll() {
        return directorService.findAll();
    }

    @GetMapping("/{directorId}")
    @ResponseStatus(HttpStatus.OK)
    public Director getDirectorById(@PathVariable Long directorId) {
        return directorService.findById(directorId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Director create(@Valid @RequestBody Director director) {
        return directorService.create(director);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Director update(@Valid @RequestBody Director director) {
        return directorService.update(director);
    }

    @DeleteMapping("/{directorId}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable Long directorId) {
        directorService.delete(directorId);
    }


}
