package com.musicapp.musicstream.controller;

import java.util.Optional;

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

import com.musicapp.musicstream.entities.Album;
import com.musicapp.musicstream.repository.AlbumRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/albums")
@Tag(name = "Album", description = "Operations related to Album")
public class AlbumController {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ArtistController artistController;

    @Operation(summary = "Create a new album")
    @PostMapping
    public ResponseEntity<Album> createAlbum(@RequestBody Album album) {

        if(albumRepository.findByTitle(album.getTitle()) != null) {
            // Retorna un error 412 si ya existe un album con el mismo nombre
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        if(album.getArtist() == null){
            // Retorna un error 400 si no se especifica ningún artista
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if(artistController.getArtistById(album.getArtist().getId()).getStatusCodeValue() == 404){
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
        }
        
        Album savedAlbum = albumRepository.save(album); // Guarda la canción en la base de datos

        return ResponseEntity.ok(savedAlbum);
    }

    @Operation(summary = "Get all albums")
    @GetMapping
    public ResponseEntity<Iterable<Album>> getAllAlbums() {
        Iterable<Album> artists = albumRepository.findAll();
        return ResponseEntity.ok(artists);
    }

    @Operation(summary = "Get album by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Album> getAlbumById(@PathVariable Integer id) {
        Optional<Album> album = albumRepository.findById(id);
        return album.map(ResponseEntity::ok)
                     .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get album by name")
    @GetMapping("/name/{name}")
    public ResponseEntity<Album> getAlbumByName(@PathVariable String name) {
        Album album = albumRepository.findByTitle(name);
        return album != null ? ResponseEntity.ok(album) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Update album")
    @PutMapping("/{id}")
    public ResponseEntity<Album> updateAlbum(@PathVariable Integer id, @RequestBody Album albumDetails) {
        Optional<Album> albumOptional = albumRepository.findById(id);
        if (!albumOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Album album = albumOptional.get();
        album.setTitle(albumDetails.getTitle());
        album.setYear(albumDetails.getYear());
        album.setDescription(albumDetails.getDescription());
        album.setNumberOfSongs(albumDetails.getNumberOfSongs());

        //album.setAlbumId(albumDetails.getAlbumId());
        album.setUrl(albumDetails.getUrl());

        Album updatedAlbum = albumRepository.save(album);
        return ResponseEntity.ok(updatedAlbum);
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
