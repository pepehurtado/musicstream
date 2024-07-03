package com.musicapp.musicstream;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.musicapp.musicstream.controller.GenreController;
import com.musicapp.musicstream.dto.DTOUtils;
import com.musicapp.musicstream.dto.GenreDTO;
import com.musicapp.musicstream.entities.Genre;
import com.musicapp.musicstream.entities.Song;
import com.musicapp.musicstream.repository.GenreRepository;
import com.musicapp.musicstream.repository.SongRepository;

public class GenreControllerTest {

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private SongRepository songRepository;

    @Mock
    private DTOUtils dtoUtil;

    @InjectMocks
    private GenreController genreController;

    private Genre genre;
    private GenreDTO genreDTO;
    private Song existingSong;
    private Song nonExistingSong;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        genre = new Genre();
        genre.setId(1);
        genre.setName("Rock");
        genre.setDescription("Rock music");
        genre.setYear(1960);
        genre.setSongList(new HashSet<>());

        genreDTO = new GenreDTO();
        genreDTO.setId(1);
        genreDTO.setName("Rock");
        genreDTO.setDescription("Rock music");
        genreDTO.setYear(1960);
        genreDTO.setSongList(new HashSet<>());

        existingSong = new Song();
        existingSong.setId(1);
        existingSong.setTitle("Existing Song");

        nonExistingSong = new Song();
        nonExistingSong.setId(2);
        nonExistingSong.setTitle("Non-Existing Song");

    }

    @Test
    void testCreateGenre() {
        when(genreRepository.findByName(anyString())).thenReturn(null);
        when(genreRepository.save(any(Genre.class))).thenReturn(genre);
        when(dtoUtil.convertToDto(any(Genre.class))).thenReturn(genreDTO);

        ResponseEntity<GenreDTO> response = genreController.createGenre(genre);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(genreDTO, response.getBody());
    }

    @Test
    void testCreateGenreAlreadyExists() {
        when(genreRepository.findByName(anyString())).thenReturn(genre);

        ResponseEntity<GenreDTO> response = genreController.createGenre(genre);
        assertEquals(HttpStatus.PRECONDITION_FAILED, response.getStatusCode());
    }

    @Test
    void testGetAllGenres() {
        List<Genre> genres = Arrays.asList(genre);
        when(genreRepository.findAll(any(Specification.class))).thenReturn(genres);
        when(dtoUtil.convertToDto(any(Genre.class))).thenReturn(genreDTO);

        ResponseEntity<List<GenreDTO>> response = genreController.getAllGenres(null, null, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(genreDTO, response.getBody().get(0));
    }

    @Test
    void testGetGenreById() {
        when(genreRepository.findById(anyInt())).thenReturn(Optional.of(genre));
        when(dtoUtil.convertToDto(any(Genre.class))).thenReturn(genreDTO);

        ResponseEntity<GenreDTO> response = genreController.getGenreById(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(genreDTO, response.getBody());
    }

    @Test
    void testGetGenreByIdNotFound() {
        when(genreRepository.findById(anyInt())).thenReturn(Optional.empty());

        ResponseEntity<GenreDTO> response = genreController.getGenreById(1);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateGenre() {
        when(genreRepository.findById(anyInt())).thenReturn(Optional.of(genre));
        when(genreRepository.save(any(Genre.class))).thenReturn(genre);
        when(dtoUtil.convertToDto(any(Genre.class))).thenReturn(genreDTO);

        ResponseEntity<GenreDTO> response = genreController.updateGenre(1, genre);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(genreDTO, response.getBody());
    }

    @Test
    void testUpdateGenreNotFound() {
        when(genreRepository.findById(anyInt())).thenReturn(Optional.empty());

        ResponseEntity<GenreDTO> response = genreController.updateGenre(1, genre);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteGenre() {
        when(genreRepository.existsById(anyInt())).thenReturn(true);
        doNothing().when(genreRepository).deleteById(anyInt());

        ResponseEntity<Void> response = genreController.deleteGenre(1);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testDeleteGenreNotFound() {
        when(genreRepository.existsById(anyInt())).thenReturn(false);

        ResponseEntity<Void> response = genreController.deleteGenre(1);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testPatchGenre() {
        when(genreRepository.findById(anyInt())).thenReturn(Optional.of(genre));
        when(genreRepository.save(any(Genre.class))).thenReturn(genre);
        when(dtoUtil.convertToDto(any(Genre.class))).thenReturn(genreDTO);

        Genre updatedGenre = new Genre();
        updatedGenre.setName("Updated Name");

        ResponseEntity<GenreDTO> response = genreController.patchGenre(1, updatedGenre);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(genreDTO, response.getBody());
    }

    @Test
    void testPatchGenreNotFound() {
        when(genreRepository.findById(anyInt())).thenReturn(Optional.empty());

        Genre updatedGenre = new Genre();
        updatedGenre.setName("Updated Name");

        ResponseEntity<GenreDTO> response = genreController.patchGenre(1, updatedGenre);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testCreateGenreWithExistingSongs() {
        genre.getSongList().add(existingSong);
    
        when(genreRepository.findByName(anyString())).thenReturn(null);
        when(songRepository.findById(anyInt())).thenReturn(Optional.of(existingSong));
        when(songRepository.existsById(anyInt())).thenReturn(true);
        when(genreRepository.save(any(Genre.class))).thenReturn(genre);
        when(dtoUtil.convertToDto(any(Genre.class))).thenReturn(genreDTO);
    
        ResponseEntity<GenreDTO> response = genreController.createGenre(genre);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(genreDTO, response.getBody());
    }

    @Test
    void testCreateGenreWithNonExistingSongs() {
        genre.getSongList().add(nonExistingSong);

        when(genreRepository.findByName(anyString())).thenReturn(null);
        when(songRepository.findById(anyInt())).thenReturn(Optional.empty());

        ResponseEntity<GenreDTO> response = genreController.createGenre(genre);
        assertEquals(HttpStatus.PRECONDITION_FAILED, response.getStatusCode());
    }

    @Test
    void testPatchGenreWithExistingSongs() {
        genre.getSongList().add(existingSong);

        when(genreRepository.findById(anyInt())).thenReturn(Optional.of(genre));
        when(songRepository.findById(anyInt())).thenReturn(Optional.of(existingSong));
        when(genreRepository.save(any(Genre.class))).thenReturn(genre);
        when(dtoUtil.convertToDto(any(Genre.class))).thenReturn(genreDTO);

        Genre updatedGenre = new Genre();
        updatedGenre.setSongList(new HashSet<>(Arrays.asList(existingSong)));

        ResponseEntity<GenreDTO> response = genreController.patchGenre(1, updatedGenre);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(genreDTO, response.getBody());
    }

    @Test
    void testPatchGenreWithNonExistingSongs() {
        genre.getSongList().add(nonExistingSong);

        when(genreRepository.findById(anyInt())).thenReturn(Optional.of(genre));
        when(songRepository.findById(anyInt())).thenReturn(Optional.empty());
        

        Genre updatedGenre = new Genre();
        updatedGenre.setSongList(new HashSet<>(Arrays.asList(nonExistingSong)));

        ResponseEntity<GenreDTO> response = genreController.patchGenre(1, updatedGenre);
        assertEquals(HttpStatus.PRECONDITION_FAILED, response.getStatusCode());
    }
}
