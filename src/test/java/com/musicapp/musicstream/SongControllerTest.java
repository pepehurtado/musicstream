package com.musicapp.musicstream;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.musicapp.musicstream.controller.SongController;
import com.musicapp.musicstream.dto.DTOUtils;
import com.musicapp.musicstream.dto.SongDTO;
import com.musicapp.musicstream.entities.Album;
import com.musicapp.musicstream.entities.Artist;
import com.musicapp.musicstream.entities.Genre;
import com.musicapp.musicstream.entities.Song;
import com.musicapp.musicstream.exception.ApiRuntimeException;
import com.musicapp.musicstream.repository.AlbumRepository;
import com.musicapp.musicstream.repository.ArtistRepository;
import com.musicapp.musicstream.repository.GenreRepository;
import com.musicapp.musicstream.repository.SongRepository;

public class SongControllerTest {

    @Mock
    private SongRepository songRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private GenreRepository genreRepository;


    @Mock
    private DTOUtils dtoUtil;

    @InjectMocks
    private SongController songController;

    private Song song;
    private SongDTO songDTO;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        song = new Song();
        song.setId(1);
        song.setTitle("Test Song");
        song.setTime(300);
        song.setUrl("http://testurl.com");

        Album album = new Album();
        album.setId(1);
        //song.setAlbum(album);

        Set<Artist> artists = new HashSet<>();
        Artist artist = new Artist();
        artist.setId(1);
        artists.add(artist);
        song.setArtists(artists);

        Set<Genre> genres = new HashSet<>();
        Genre genre = new Genre();
        genre.setId(1);
        genres.add(genre);
        song.setGenreList(genres);


        songDTO = new SongDTO();
        songDTO.setId(1);
        songDTO.setTitle("Test Song");
        songDTO.setTime(300);
        songDTO.setUrl("http://testurl.com");

        Integer artistDTO = 1;


        Set<Integer> artistsId = new HashSet<>();
        artistsId.add(1);
        songDTO.setArtists(artistsId);
        Set<Integer> genreIds = new HashSet<>();
        genreIds.add(artistDTO);
        songDTO.setGenreList(genreIds);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetAllSongs() {
        //Creamos un hashset de canciones y añadimos una
        List<Song> songs = new ArrayList<>();
        songs.add(song);

        when(songRepository.findAll(any(Specification.class))).thenReturn(songs);
        when(dtoUtil.convertToDto(song)).thenReturn(songDTO);

        ResponseEntity<List<SongDTO>> response = songController.getAllSongs(null,null,null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testGetSongById() {
        when(songRepository.findById(1)).thenReturn(Optional.of(song));
        when(dtoUtil.convertToDto(song)).thenReturn(songDTO);
        when(songRepository.existsById(1)).thenReturn(true);
        ResponseEntity<SongDTO> response = songController.getSongById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(songDTO.getId(), response.getBody().getId());
    }

    @Test
    public void testCreateSongWithoutAlbum() {
        when(songRepository.findByTitle("Test Song")).thenReturn(null);
        when(albumRepository.findById(1)).thenReturn(null);
        when(artistRepository.findById(1)).thenReturn(song.getArtists().stream().findFirst());
        when(genreRepository.findById(1)).thenReturn(song.getGenreList().stream().findFirst());

        when(songRepository.save(song)).thenReturn(song);
        when(dtoUtil.convertToDto(song)).thenReturn(songDTO);
        ResponseEntity<?> response = songController.createSong(song);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(songDTO, response.getBody());
    }

    @Test
    public void testUpdateTitleSong() {
        when(songRepository.findById(1)).thenReturn(Optional.of(song));
        when(songRepository.save(song)).thenReturn(song);
        when(dtoUtil.convertToDto(song)).thenReturn(songDTO);
        when(artistRepository.findById(1)).thenReturn(song.getArtists().stream().findFirst());
        when(genreRepository.findById(1)).thenReturn(song.getGenreList().stream().findFirst());
        songDTO.setTitle("Updated Title");
        when(songRepository.existsById(1)).thenReturn(true);

        ResponseEntity<SongDTO> response = songController.updateSong(1, song);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Title", response.getBody().getTitle());
    }

    @Test
    public void testUpdateAddArtistSong() {
        when(songRepository.findById(1)).thenReturn(Optional.of(song));
        when(songRepository.save(song)).thenReturn(song);
        when(dtoUtil.convertToDto(song)).thenReturn(songDTO);
        when(artistRepository.findById(1)).thenReturn(song.getArtists().stream().findFirst());
        when(genreRepository.findById(1)).thenReturn(song.getGenreList().stream().findFirst());
        songDTO.setArtists(new HashSet<>());
        songDTO.getArtists().add(2);
        when(songRepository.existsById(1)).thenReturn(true);

        ResponseEntity<SongDTO> response = songController.updateSong(1, song);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getArtists().stream().findFirst().get());
    }

    @Test
    public void testDeleteSong() {
        when(songRepository.existsById(1)).thenReturn(true);

        ResponseEntity<Void> response = songController.deleteSong(1);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(songRepository).deleteById(1);
    }

    @Test
    public void testGetSongByName() {
        when(songRepository.findByTitle("Test Song")).thenReturn(song);
        when(dtoUtil.convertToDto(song)).thenReturn(songDTO);

        ResponseEntity<SongDTO> response = songController.getSongByName("Test Song");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Song", response.getBody().getTitle());
    }

    @Test
    public void createSameSong() {
        when(songRepository.findByTitle("Test Song")).thenReturn(song);
        //Manejar la excepción de que ya existe la canción
        
        try {
            songController.createSong(song);
        } catch (Exception e) {
            //Comprobar que es exactamente la excepción que esperamos
            assertEquals("This song name already exists " + song.getTitle(), e.getMessage());
            assertEquals(409, ((ApiRuntimeException) e).getStatusCode());
        }

    }
}
