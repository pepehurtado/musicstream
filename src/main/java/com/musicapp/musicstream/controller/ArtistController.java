package com.musicapp.musicstream.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.musicapp.musicstream.entities.Artist;
import com.musicapp.musicstream.repository.ArtistRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/artists")
@Tag(name = "Artist", description = "Operations related to Artists")
public class ArtistController {

    @Autowired
    private ArtistRepository artistRepository;

    @Operation(summary = "Create a new artist")
    @PostMapping
    public ResponseEntity<Artist> createArtist(@RequestBody Artist artist) {
        Artist savedArtist = artistRepository.save(artist);
        return ResponseEntity.ok(savedArtist);
    }

    @Operation(summary = "Get all artists")
    @GetMapping
    public ResponseEntity<Iterable<Artist>> getAllArtists() {
        Iterable<Artist> artists = artistRepository.findAll();
        return ResponseEntity.ok(artists);
    }

    @Operation(summary = "Get artist by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Artist> getArtistById(@PathVariable Integer id) {
        Optional<Artist> artist = artistRepository.findById(id);
        return artist.map(ResponseEntity::ok)
                     .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get artist by name")
    @GetMapping("/name/{name}")
    public ResponseEntity<Artist> getArtistByName(@PathVariable String name) {
        Artist artist = artistRepository.findByName(name);
        return artist != null ? ResponseEntity.ok(artist) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Update artist")
    @PutMapping("/{id}")
    public ResponseEntity<Artist> updateArtist(@PathVariable Integer id, @RequestBody Artist artistDetails) {
        Optional<Artist> artistOptional = artistRepository.findById(id);
        if (!artistOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Artist artist = artistOptional.get();
        artist.setName(artistDetails.getName());
        artist.setAge(artistDetails.getAge());

        Artist updatedArtist = artistRepository.save(artist);
        return ResponseEntity.ok(updatedArtist);
    }

    @Operation(summary = "Delete artist")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArtist(@PathVariable Integer id) {
        if (!artistRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        artistRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
