package com.musicapp.musicstream;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import com.musicapp.musicstream.common.ArtistSpecification;
import com.musicapp.musicstream.controller.ArtistController;
import com.musicapp.musicstream.dto.ArtistDTO;
import com.musicapp.musicstream.dto.DTOUtils;
import com.musicapp.musicstream.entities.Artist;
import com.musicapp.musicstream.exception.ApiRuntimeException;
import com.musicapp.musicstream.repository.ArtistRepository;

public class ArtistControllerTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private DTOUtils dtoUtil;

    @InjectMocks
    private ArtistController artistController;


    private List<Artist> artists;
    private List<ArtistDTO> artistDTOs;
    private Artist artist;
    private ArtistDTO artistDTO;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        artists = new ArrayList<>();
        artistDTOs = new ArrayList<>();

        artist = new Artist();
        artist.setId(1);
        artist.setName("John Doe");
        artist.setDateOfBirth(Date.valueOf("1990-01-01"));
        artist.setAge(31);
        artists.add(artist);

        artistDTO = new ArtistDTO();
        artistDTO.setId(1);
        artistDTO.setName("John Doe");
        artistDTO.setDateOfBirth(Date.valueOf("1990-01-01"));
        artistDTO.setAge(31);
        artistDTOs.add(artistDTO);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testGetAllArtists() {
        // Configurar comportamiento de Mockito para el repositorio
        Specification<Artist> spec = Specification.where(ArtistSpecification.hasName(null))
                .and(ArtistSpecification.hasCountry(null))
                .and(ArtistSpecification.hasAge(null))
                .and(ArtistSpecification.hasDateOfBirth(null));
        
        when(artistRepository.findAll(spec)).thenReturn(artists);
        when(artistRepository.findAll()).thenReturn(artists);

        List<Artist> artistsList = new ArrayList<>();
        artistsList.add(artist);

        when(artistRepository.findAll(any(Specification.class))).thenReturn(artistsList);
        when(dtoUtil.convertToDto(artist)).thenReturn(artistDTO);

        ResponseEntity<List<ArtistDTO>> response = artistController.getAllArtists(null, null, null, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testGetArtistById() {
        when(artistRepository.findById(1)).thenReturn(Optional.of(artist));
        when(dtoUtil.convertToDto(artist)).thenReturn(artistDTO);
        when(artistRepository.existsById(1)).thenReturn(true);

        ResponseEntity<ArtistDTO> response = artistController.getArtistById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(artistDTO.getId(), response.getBody().getId());
    }

    @Test
    public void testCreateArtist() {
        when(artistRepository.findByName("John Doe")).thenReturn(null);

        ResponseEntity<ArtistDTO> response = artistController.createArtist(artist);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(artistDTO.getId(), response.getBody().getId());
    }

    @Test
    public void testUpdateArtist() {
        when(artistRepository.findById(1)).thenReturn(Optional.of(artist));
        when(artistRepository.save(artist)).thenReturn(artist);
        when(dtoUtil.convertToDto(artist)).thenReturn(artistDTO);
        when(artistRepository.existsById(1)).thenReturn(true);
        artistDTO.setName("Updated Name");

        ResponseEntity<ArtistDTO> response = artistController.updateArtist(1, artistDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Name", response.getBody().getName());
    }

    @Test
    public void testDeleteArtist() {
        when(artistRepository.existsById(1)).thenReturn(true);

        ResponseEntity<Void> response = artistController.deleteArtist(1);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(artistRepository).deleteById(1);
    }

    @Test
    public void testGetArtistByName() {
        when(artistRepository.findByName("John Doe")).thenReturn(artist);
        when(dtoUtil.convertToDto(artist)).thenReturn(artistDTO);

        ResponseEntity<ArtistDTO> response = artistController.getArtistByName("John Doe");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Doe", response.getBody().getName());
    }

    @Test
    public void createSameArtist() {
        when(artistRepository.findByName("John Doe")).thenReturn(artist);
    
        try {
            artistController.createArtist(artist);
        } catch (Exception e) {
            assertEquals("Artist already exists " +artist.getName(), e.getMessage());
            //Ver el error 409
            assertEquals(409, ((ApiRuntimeException) e).getStatusCode());
        }
    }
    
}
