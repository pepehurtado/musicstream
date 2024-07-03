package com.musicapp.musicstream.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.musicapp.musicstream.common.GenreSpecification;
import com.musicapp.musicstream.dto.DTOUtils;
import com.musicapp.musicstream.dto.GenreDTO;
import com.musicapp.musicstream.entities.FilterStruct;
import com.musicapp.musicstream.entities.Genre;
import com.musicapp.musicstream.entities.Song;
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
    public ResponseEntity<GenreDTO> createGenre(@RequestBody Genre genreDTO) {
        // Verificar si ya existe un género con el mismo nombre
        if (genreRepository.findByName(genreDTO.getName()) != null) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
        }

        //Crear Genre y añadir las canciones que vienen por id
        Genre genre = new Genre();
        genre.setName(genreDTO.getName());
        genre.setDescription(genreDTO.getDescription());
        genre.setYear(genreDTO.getYear());
        if (genreDTO.getSongList() != null) {
            //Si no existe la cancion que devuelva un error 412
            for (Song song : genreDTO.getSongList()) {
                if (!songRepository.existsById(song.getId())) {
                    return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
                }
                genre.addSong(songRepository.findById(song.getId()).get());
            }
        }


        Genre savedGenre = genreRepository.save(genre);
        GenreDTO genreDTOResponse = dtoUtil.convertToDto(savedGenre);
        return ResponseEntity.ok(genreDTOResponse);
    }

    @Operation(summary = "Get all genres")
    @GetMapping
    public ResponseEntity<List<GenreDTO>> getAllGenres(@RequestParam(required = false) String name,
                                                        @RequestParam(required = false) Integer year,
                                                        @RequestParam(required = false) String description) {
        Specification<Genre> spec = Specification.where(GenreSpecification.hasName(name))
                                                  .and(GenreSpecification.hasYear(year))
                                                  .and(GenreSpecification.hasDescription(description));
        List<Genre> genres = (List<Genre>) genreRepository.findAll(spec);
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


            @Operation(summary = "Get artist by dynamic filter")
    @PostMapping("/filter")
    public ResponseEntity<?> filterBy(@RequestBody FilterStruct struct) {

        // Construir el objeto Sort a partir de los criterios de ordenación
        Sort sort = Sort.unsorted();
        for (FilterStruct.SortCriteria sortCriteria : struct.getListOrderCriteria()) {
            Sort.Direction direction = sortCriteria.getValuesorOrder() == FilterStruct.SortValue.ASC ? 
                                       Sort.Direction.ASC : Sort.Direction.DESC;
            sort = sort.and(Sort.by(direction, sortCriteria.getSortBy()));
        }

        // Construir el objeto Pageable a partir de la información de paginación y ordenación
        Pageable pageable = PageRequest.of(struct.getPage().getPageIndex(), struct.getPage().getPageSize(), sort);

        // Construir la especificación a partir de los criterios de búsqueda
        Specification<Genre> specification = GenreSpecification.getGenresByFilters(struct.getListSearchCriteria());

        // Realizar la consulta con el repositorio utilizando Pageable y Specification
        Page<Genre> genres = genreRepository.findAll(specification, pageable);

        // Convertir a DTO
        List<GenreDTO> genreDTOs = genres.stream()
                                            .map(dtoUtil::convertToDto)
                                            .collect(Collectors.toList());

        return ResponseEntity.ok(genreDTOs);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GenreDTO> patchGenre(@PathVariable Integer id, @RequestBody Genre genreDetails) {
        Optional<Genre> genreOptional = genreRepository.findById(id);
        if (!genreOptional.isPresent()) {
             return ResponseEntity.status(HttpStatus.CONFLICT).build();
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
            for (Song song : genreDetails.getSongList()) {
                Optional<Song> songOptional = songRepository.findById(song.getId());
                if (!songOptional.isPresent()) {
                    return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
                }
                genre.addSong(songOptional.get());
            }
        }

        Genre updatedGenre = genreRepository.save(genre);
        GenreDTO genreDTO = dtoUtil.convertToDto(updatedGenre);
        return ResponseEntity.ok(genreDTO);
    }
}
