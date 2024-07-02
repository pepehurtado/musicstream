package com.musicapp.musicstream.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.musicapp.musicstream.dto.DTOUtils;
import com.musicapp.musicstream.dto.GenreDTO;
import com.musicapp.musicstream.entities.Genre;
import com.musicapp.musicstream.repository.GenreRepository;
import com.musicapp.musicstream.repository.SongRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/genres")
@Tag(name = "Genre", description = "Operations related to Genre")
public class GenreController {

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private DTOUtils dtoUtil;

    @Operation(summary = "Create a new genre")
    @PostMapping
    public ResponseEntity<GenreDTO> createGenre(@RequestBody Genre genre) {
        // Verificar si ya existe un género con el mismo nombre
        if (genreRepository.findByName(genre.getName()) != null) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
        }

        Genre savedGenre = genreRepository.save(genre);
        GenreDTO genreDTO = dtoUtil.convertToDto(savedGenre);
        return ResponseEntity.ok(genreDTO);
    }

    @Operation(summary = "Get all genres")
    @GetMapping
    public ResponseEntity<List<GenreDTO>> getAllGenres() {
        List<Genre> genres = (List<Genre>) genreRepository.findAll();
        List<GenreDTO> genresDTO = genres.stream()
                                        .map(dtoUtil::convertToDto)
                                        .collect(Collectors.toList());
        return ResponseEntity.ok(genresDTO);
    }

    @Operation(summary = "Get genre by ID")
    @GetMapping("/{id}")
    public ResponseEntity<GenreDTO> getGenreById(@PathVariable Integer id) {
        Optional<Genre> genre = genreRepository.findById(id);
        GenreDTO genreDTO = genre.map(dtoUtil::convertToDto)
                                .orElse(null);
        return genreDTO != null ? ResponseEntity.ok(genreDTO) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Update genre")
    @PutMapping("/{id}")
    public ResponseEntity<GenreDTO> updateGenre(@PathVariable Integer id, @RequestBody Genre genreDetails) {
        Optional<Genre> genreOptional = genreRepository.findById(id);
        if (!genreOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Genre genre = genreOptional.get();
        genre.setName(genreDetails.getName());
        genre.setDescription(genreDetails.getDescription());
        genre.setYear(genreDetails.getYear());

        Genre updatedGenre = genreRepository.save(genre);
        GenreDTO genreDTO = dtoUtil.convertToDto(updatedGenre);
        return ResponseEntity.ok(genreDTO);
    }

    @Operation(summary = "Delete genre")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable Integer id) {
        if (!genreRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        genreRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GenreDTO> patchGenre(@PathVariable Integer id, @RequestBody Genre genreDetails) {
        Optional<Genre> genreOptional = genreRepository.findById(id);
        if (!genreOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Genre genre = genreOptional.get();
        if (genreDetails.getName() != null) {
            genre.setName(genreDetails.getName());
        }
        if (genreDetails.getDescription() != null) {
            genre.setDescription(genreDetails.getDescription());
        }
        if (genreDetails.getYear() != null) {
            genre.setYear(genreDetails.getYear());
        }
        //Actualizar canciones
        if (genreDetails.getSongList() != null) {
            //Limpiar la lista de canciones
            genre.getSongList().clear();
            //Agregar las nuevas canciones gracias al id de la cancion
            genreDetails.getSongList().forEach(song-> {
                genre.addSong(songRepository.findById(song.getId()).get());
            });
        }

        Genre updatedGenre = genreRepository.save(genre);
        GenreDTO genreDTO = dtoUtil.convertToDto(updatedGenre);
        return ResponseEntity.ok(genreDTO);
    }
}
