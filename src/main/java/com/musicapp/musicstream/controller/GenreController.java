package com.musicapp.musicstream.controller;

import com.musicapp.musicstream.dto.GenreDTO;
import com.musicapp.musicstream.entities.Genre;
import com.musicapp.musicstream.entities.FilterStruct;
import com.musicapp.musicstream.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/genres")
@Tag(name = "Genre", description = "Operations related to Genre")
public class GenreController {

    @Autowired
    private GenreService genreService;

    @Operation(summary = "Create a new genre")
    @PostMapping
    public ResponseEntity<GenreDTO> createGenre(@RequestBody Genre genre) {
        GenreDTO createdGenre = genreService.createGenre(genre);
        return ResponseEntity.status(201).body(createdGenre);
    }

    @Operation(summary = "Get all genres")
    @GetMapping
    public ResponseEntity<List<GenreDTO>> getAllGenres(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String description) {
        List<GenreDTO> genres = genreService.getAllGenres(name, year, description);
        return ResponseEntity.ok(genres);
    }

    @Operation(summary = "Get genre by ID")
    @GetMapping("/{id}")
    public ResponseEntity<GenreDTO> getGenreById(@PathVariable Integer id) {
        GenreDTO genre = genreService.getGenreById(id);
        return ResponseEntity.ok(genre);
    }

    @Operation(summary = "Update genre")
    @PutMapping("/{id}")
    public ResponseEntity<GenreDTO> updateGenre(@PathVariable Integer id, @RequestBody Genre genre) {
        GenreDTO updatedGenre = genreService.updateGenre(id, genre);
        return ResponseEntity.ok(updatedGenre);
    }

    @Operation(summary = "Delete genre")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable Integer id) {
        genreService.deleteGenre(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get genre by dynamic filter")
    @PostMapping("/filter")
    public ResponseEntity<?> filterBy(@RequestBody FilterStruct filter) {
        List<GenreDTO> genres = genreService.filterGenres(filter);
        return ResponseEntity.ok(genres);
    }

    @Operation(summary = "Patch genre")
    @PatchMapping("/{id}")
    public ResponseEntity<GenreDTO> patchGenre(@PathVariable Integer id, @RequestBody Genre genre) {
        GenreDTO updatedGenre = genreService.patchGenre(id, genre);
        return ResponseEntity.ok(updatedGenre);
    }
}