package com.musicapp.musicstream;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.musicapp.musicstream.controller.AlbumController;
import com.musicapp.musicstream.dto.AlbumDTO;
import com.musicapp.musicstream.dto.DTOUtils;
import com.musicapp.musicstream.dto.SongDTO;
import com.musicapp.musicstream.entities.Album;
import com.musicapp.musicstream.entities.Artist;
import com.musicapp.musicstream.entities.Song;
import com.musicapp.musicstream.repository.AlbumRepository;
import com.musicapp.musicstream.repository.ArtistRepository;
import com.musicapp.musicstream.repository.GenreRepository;
import com.musicapp.musicstream.repository.SongRepository;

public class AlbumControllerTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private SongRepository songRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private DTOUtils dtoUtil;

    @InjectMocks
    private AlbumController albumController;

    private HashSet<Album> albums;
    private HashSet<AlbumDTO> albumDTOs;
    private Album album;
    private AlbumDTO albumDTO;

    private Artist artist;
    private Song song;
    private SongDTO songDTO;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Set<Album> albums = new HashSet<>();
        Set<AlbumDTO> albumDTOs = new HashSet<>();

        album = new Album();
        album.setId(1);
        album.setTitle("Test Album");
        album.setYear("2024");

        artist = new Artist();
        artist.setId(1);
        album.setArtist(artist);

        Set<Song> songs = new HashSet<>();
        song = new Song();
        song.setId(1);
        song.setTitle("Test Song");
        song.setTime(300);
        song.setUrl("http://testurl.com");
        songs.add(song);
        album.setSongs(songs);

        albums.add(album);

        albumDTO = new AlbumDTO();
        albumDTO.setId(1);
        albumDTO.setTitle("Test Album");
        albumDTO.setYear("2024");

        albumDTOs.add(albumDTO);
    }

    @Test
    public void testGetAllAlbums() {
        List<Album> albums = new ArrayList<>();
        albums.add(album);

        when(albumRepository.findAll(any(Specification.class))).thenReturn(albums);
        when(dtoUtil.convertToDto(album)).thenReturn(albumDTO);

        ResponseEntity<List<AlbumDTO>> response = albumController.getAllAlbums(null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testGetAlbumById() {
        when(albumRepository.findById(1)).thenReturn(Optional.of(album));
        when(dtoUtil.convertToDto(album)).thenReturn(albumDTO);

        ResponseEntity<AlbumDTO> response = albumController.getAlbumById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(albumDTO.getId(), response.getBody().getId());
    }

    @Test
    public void testCreateAlbum() {
        when(albumRepository.findByTitle("Test Album")).thenReturn(null);
        when(artistRepository.findById(1)).thenReturn(Optional.of(artist));
        when(songRepository.findByTitle("Test Song")).thenReturn(song);

        when(albumRepository.save(album)).thenReturn(album);
        when(dtoUtil.convertToDto(album)).thenReturn(albumDTO);

        ResponseEntity<?> response = albumController.createAlbum(album);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testUpdateAlbum() {
        when(albumRepository.findById(1)).thenReturn(Optional.of(album));
        when(albumRepository.save(album)).thenReturn(album);
        when(dtoUtil.convertToDto(album)).thenReturn(albumDTO);

        albumDTO.setTitle("Updated Title");

        ResponseEntity<AlbumDTO> response = albumController.updateAlbum(1, albumDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Title", response.getBody().getTitle());
    }

    @Test
    public void testDeleteAlbum() {
        when(albumRepository.existsById(1)).thenReturn(true);

        ResponseEntity<Void> response = albumController.deleteAlbum(1);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(albumRepository).deleteById(1);
    }

    @Test
    public void testGetAlbumByName() {
        when(albumRepository.findByTitle("Test Album")).thenReturn(album);
        when(dtoUtil.convertToDto(album)).thenReturn(albumDTO);

        ResponseEntity<AlbumDTO> response = albumController.getAlbumByName("Test Album");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Album", response.getBody().getTitle());
    }

    @Test
    public void testCreateSameAlbum() {
        when(albumRepository.findByTitle("Test Album")).thenReturn(album);

        ResponseEntity<?> response = albumController.createAlbum(album);

        assertEquals(HttpStatus.PRECONDITION_FAILED, response.getStatusCode());
    }
}
