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
import com.musicapp.musicstream.exception.ApiRuntimeException;
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
    public void setUp() {
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

        try {
            genreController.createGenre(genre);
        } catch (Exception e) {
            //Comprobar tambien que se trata de un error 409
            assertEquals(ApiRuntimeException.class, e.getClass());
            assertEquals("Genre already exists: " + genre.getName(), e.getMessage());
            //Suponer que Exception e es de tipo ApiRuntimeException para hacer un getStatusCode
            assertEquals(409, ((ApiRuntimeException) e).getStatusCode());
        }
    }

    @SuppressWarnings("unchecked")
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
        when(genreRepository.existsById(anyInt())).thenReturn(true);
    }

    @Test
    void testGetGenreByIdNotFound() {
        when(genreRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(genreRepository.existsById(anyInt())).thenReturn(false);

        try {
            genreController.getGenreById(1);
        } catch (Exception e) {
            assertEquals(ApiRuntimeException.class, e.getClass());
            assertEquals("Genre not found with id : " + 1, e.getMessage());
            assertEquals(404, ((ApiRuntimeException) e).getStatusCode());
        }
    }

    @Test
    void testUpdateGenre() {
        when(genreRepository.findById(anyInt())).thenReturn(Optional.of(genre));
        when(genreRepository.save(any(Genre.class))).thenReturn(genre);
        when(dtoUtil.convertToDto(any(Genre.class))).thenReturn(genreDTO);
        when(genreRepository.existsById(anyInt())).thenReturn(true);
        ResponseEntity<GenreDTO> response = genreController.updateGenre(1, genre);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(genreDTO, response.getBody());
    }

    @Test
    void testUpdateGenreNotFound() {
        when(genreRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(genreRepository.existsById(anyInt())).thenReturn(false);

        try {
            genreController.updateGenre(1, genre);
        } catch (Exception e) {
            assertEquals(ApiRuntimeException.class, e.getClass());
            assertEquals("Genre not found with id : " + 1, e.getMessage());
            assertEquals(404, ((ApiRuntimeException) e).getStatusCode());
        }
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

        try {
            genreController.deleteGenre(1);
        } catch (Exception e) {
            assertEquals(ApiRuntimeException.class, e.getClass());
            assertEquals("Genre not found with id : " + 1, e.getMessage());
            assertEquals(404, ((ApiRuntimeException) e).getStatusCode());
        }
    }

    @Test
    void testPatchGenre() {
        when(genreRepository.findById(anyInt())).thenReturn(Optional.of(genre));
        when(genreRepository.save(any(Genre.class))).thenReturn(genre);
        when(dtoUtil.convertToDto(any(Genre.class))).thenReturn(genreDTO);
        when(genreRepository.existsById(anyInt())).thenReturn(true);

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

        try {
            genreController.patchGenre(1, updatedGenre);
        } catch (Exception e) {
            assertEquals(ApiRuntimeException.class, e.getClass());
            assertEquals("Genre not found with id : " + 1, e.getMessage());
            assertEquals(404, ((ApiRuntimeException) e).getStatusCode());
        }
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
        when(genreRepository.existsById(anyInt())).thenReturn(true);
        when(genreRepository.findByName(anyString())).thenReturn(null);
        when(songRepository.findById(anyInt())).thenReturn(Optional.empty());
        try {
            genreController.createGenre(genre);
        } catch (Exception e) {
            assertEquals(ApiRuntimeException.class, e.getClass());
            assertEquals("Song not found with id : " + nonExistingSong.getId(), e.getMessage());
            assertEquals(412, ((ApiRuntimeException) e).getStatusCode());
        }
    }

    @Test
    void testPatchGenreWithExistingSongs() {
        genre.getSongList().add(existingSong);
        when(genreRepository.existsById(anyInt())).thenReturn(true);
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
        when(genreRepository.existsById(anyInt())).thenReturn(true);
        when(genreRepository.findById(anyInt())).thenReturn(Optional.of(genre));
        when(songRepository.findById(anyInt())).thenReturn(Optional.empty());
        

        Genre updatedGenre = new Genre();
        updatedGenre.setSongList(new HashSet<>(Arrays.asList(nonExistingSong)));

        try {
            genreController.patchGenre(1, updatedGenre);
        } catch (Exception e) {
            assertEquals(ApiRuntimeException.class, e.getClass());
            assertEquals("Song not found with id : " + nonExistingSong.getId(), e.getMessage());
            assertEquals(412, ((ApiRuntimeException) e).getStatusCode());
        }
    }
}
