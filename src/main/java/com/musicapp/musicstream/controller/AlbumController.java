package com.musicapp.musicstream.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.musicapp.musicstream.dto.AlbumDTO;
import com.musicapp.musicstream.dto.DTOUtils;
import com.musicapp.musicstream.entities.Album;
import com.musicapp.musicstream.entities.Song;
import com.musicapp.musicstream.repository.AlbumRepository;
import com.musicapp.musicstream.repository.ArtistRepository;
import com.musicapp.musicstream.repository.SongRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/albums")
@Tag(name = "Album", description = "Operations related to Album")
public class AlbumController {

    @Autowired
    private AlbumRepository albumRepository;


    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private DTOUtils dtoUtil;

    @Operation(summary = "Create a new album")
    @PostMapping
    public ResponseEntity<?> createAlbum(@RequestBody Album album) {
        
        // Si ya existe un álbum con el mismo nombre no se puede crear
        if (albumRepository.findByTitle(album.getTitle()) != null) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("Ya existe un álbum con el nombre " + album.getTitle());
        }

        if (album.getArtist() == null || artistRepository.findById(album.getArtist().getId()).isEmpty()) {
            // Retorna un error 412 si el artista no existe
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("El artista con ID " + album.getArtist().getId() + " no existe");
        }

        Set<Song> existingSongs = new HashSet<>();
        if (album.getSongs() != null && !album.getSongs().isEmpty()) {
            for (Song song : album.getSongs()) {
                // Comprobar que la canción existe
                Song existingSong = songRepository.findById(song.getId()).orElse(null);
                if (existingSong == null) {
                    return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("La canción con ID " + song.getId() + " no existe");
                }
                existingSongs.add(existingSong);
            }
        }

        album.setSongs(existingSongs);
        album.setNumberOfSongs(existingSongs.size());
        Album savedAlbum = albumRepository.save(album); // Guarda el álbum en la base de datos
        System.out.println("Album saved: " + savedAlbum.getSongs());
        // Añadir la relación con las canciones
        for (Song song : existingSongs) {
            song.setAlbum(savedAlbum);
            songRepository.save(song);
        }
        //Creamos el DTO del album
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all albums")
    @GetMapping
    public ResponseEntity<List<AlbumDTO>> getAllAlbums() {
        List<Album> artists = (List<Album>) albumRepository.findAll();
        List<AlbumDTO> artistDTOs = artists.stream()
                                             .map(dtoUtil::convertToDto)
                                             .collect(Collectors.toList());
        return ResponseEntity.ok(artistDTOs);
    }

    @Operation(summary = "Get album by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AlbumDTO> getAlbumById(@PathVariable Integer id) {
        Optional<Album> album = albumRepository.findById(id);
        //Creamos el dto a partir del album
        AlbumDTO albumDTO = album.map(dtoUtil::convertToDto)
                                 .orElse(null);
        return albumDTO != null ? ResponseEntity.ok(albumDTO) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Get album by name")
    @GetMapping("/name/{name}")
    public ResponseEntity<AlbumDTO> getAlbumByName(@PathVariable String name) {
        Album album = albumRepository.findByTitle(name);
        AlbumDTO albumDTO = dtoUtil.convertToDto(album);
        return album != null ? ResponseEntity.ok(albumDTO) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Update album")
    @PutMapping("/{id}")
    public ResponseEntity<AlbumDTO> updateAlbum(@PathVariable Integer id, @RequestBody AlbumDTO albumDetailsDTO) {
        Optional<Album> albumOptional = albumRepository.findById(id);
        if (!albumOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Album album = albumOptional.get();
        album.setTitle(albumDetailsDTO.getTitle());
        album.setYear(albumDetailsDTO.getYear());
        album.setDescription(albumDetailsDTO.getDescription());
        album.setNumberOfSongs(albumDetailsDTO.getNumberOfSongs());

        //album.setAlbumId(albumDetailsDTO.getAlbumId());
        album.setUrl(albumDetailsDTO.getUrl());

        Album updatedAlbum = albumRepository.save(album);
        albumDetailsDTO.setId(updatedAlbum.getId());
        return ResponseEntity.ok(albumDetailsDTO);
    }

    @Operation(summary = "Delete album")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable Integer id) {
        if (!albumRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        albumRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
