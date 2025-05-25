package com.musicapp.musicstream.controller;

import com.musicapp.musicstream.dto.ArtistDTO;
import com.musicapp.musicstream.entities.Artist;
import com.musicapp.musicstream.entities.FilterStruct;
import com.musicapp.musicstream.service.ArtistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/artists")
@Tag(name = "Artist", description = "Operations related to Artists")
public class ArtistController {

    @Autowired
    private ArtistService artistService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new artist")
    @PostMapping
    public ResponseEntity<ArtistDTO> createArtist(@RequestBody Artist artist) {
        ArtistDTO createdArtist = artistService.createArtist(artist);
        return ResponseEntity.ok(createdArtist);
    }

    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    @Operation(summary = "Get all artists")
    @GetMapping
    public ResponseEntity<List<ArtistDTO>> getAllArtists(
            @RequestParam(required = false, name = "name") String name,
            @RequestParam(required = false, name = "country") String country,
            @RequestParam(required = false, name = "age") Integer age,
            @RequestParam(required = false, name = "dateOfBirth") String dateOfBirth) {
        List<ArtistDTO> artists = artistService.getAllArtists(name, country, age, dateOfBirth);
        return ResponseEntity.ok(artists);
    }

    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    @Operation(summary = "Get artist by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ArtistDTO> getArtistById(@PathVariable Integer id) {
        ArtistDTO artist = artistService.getArtistById(id);
        return ResponseEntity.ok(artist);
    }

    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    @Operation(summary = "Get artist by name")
    @GetMapping("/name/{name}")
    public ResponseEntity<ArtistDTO> getArtistByName(@PathVariable String name) {
        ArtistDTO artist = artistService.getArtistByName(name);
        return ResponseEntity.ok(artist);
    }

    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    @Operation(summary = "Update artist")
    @PutMapping("/{id}")
    public ResponseEntity<ArtistDTO> updateArtist(@PathVariable Integer id, @RequestBody ArtistDTO artistDTO) {
        ArtistDTO updatedArtist = artistService.updateArtist(id, artistDTO);
        return ResponseEntity.ok(updatedArtist);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete artist")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArtist(@PathVariable Integer id) {
        artistService.deleteArtist(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    @Operation(summary = "Get artist by dynamic filter")
    @PostMapping("/filter")
    public ResponseEntity<?> filterBy(@RequestBody FilterStruct filter) {
        Page<ArtistDTO> artists = artistService.filterArtists(filter);
        return ResponseEntity.ok(artists);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<ArtistDTO> patchArtist(@PathVariable Integer id, @RequestBody Artist artist) {
        ArtistDTO updatedArtist = artistService.patchArtist(id, artist);
        return ResponseEntity.ok(updatedArtist);
    }
}