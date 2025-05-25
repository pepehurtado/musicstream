package com.musicapp.musicstream.controller;

import com.musicapp.musicstream.dto.AlbumDTO;
import com.musicapp.musicstream.entities.Album;
import com.musicapp.musicstream.entities.FilterStruct;
import com.musicapp.musicstream.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/albums")
@Tag(name = "Album", description = "Operations related to Album")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @Operation(summary = "Create a new album")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAlbum(@RequestBody Album album) {
        albumService.createAlbum(album);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all albums")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    public ResponseEntity<List<AlbumDTO>> getAllAlbums(
            @RequestParam(required = false, name = "title") String title,
            @RequestParam(required = false, name = "year") Integer year,
            @RequestParam(required = false, name = "numberOfSongs") Integer numberOfSongs,
            @RequestParam(required = false, name = "url") String url) {
        List<AlbumDTO> albums = albumService.getAllAlbums(title, year, numberOfSongs, url);
        return ResponseEntity.ok(albums);
    }

    @Operation(summary = "Get album by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AlbumDTO> getAlbumById(@PathVariable Integer id) {
        AlbumDTO album = albumService.getAlbumById(id);
        return ResponseEntity.ok(album);
    }

    @PreAuthorize("hasRole('ADMIN')|| hasRole('USER')")
    @Operation(summary = "Get album by name")
    @GetMapping("/name/{name}")
    public ResponseEntity<AlbumDTO> getAlbumByName(@PathVariable String name) {
        AlbumDTO album = albumService.getAlbumByName(name);
        return ResponseEntity.ok(album);
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    @Operation(summary = "Update album")
    @PutMapping("/{id}")
    public ResponseEntity<AlbumDTO> updateAlbum(@PathVariable Integer id, @RequestBody AlbumDTO albumDetailsDTO) {
        AlbumDTO updatedAlbum = albumService.updateAlbum(id, albumDetailsDTO);
        return ResponseEntity.ok(updatedAlbum);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete album")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable Integer id) {
        albumService.deleteAlbum(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')|| hasRole('USER')")
    @Operation(summary = "Get album by dynamic filter")
    @PostMapping("/filter")
    public ResponseEntity<?> filterBy(@RequestBody FilterStruct struct) {
        List<AlbumDTO> albums = albumService.filterAlbums(struct);
        return ResponseEntity.ok(albums);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<AlbumDTO> patchAlbum(@PathVariable Integer id, @RequestBody Album albumDetails) {
        AlbumDTO updatedAlbum = albumService.patchAlbum(id, albumDetails);
        return ResponseEntity.ok(updatedAlbum);
    }
}