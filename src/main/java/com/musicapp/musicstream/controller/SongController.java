package com.musicapp.musicstream.controller;

import com.musicapp.musicstream.dto.PagedResponse;
import com.musicapp.musicstream.dto.SongDTO;
import com.musicapp.musicstream.entities.FilterStruct;
import com.musicapp.musicstream.entities.Song;
import com.musicapp.musicstream.service.SongService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/songs")
@Tag(name = "Song", description = "Operations related to Song")
public class SongController {

    @Autowired
    private SongService songService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new song")
    @PostMapping
    public ResponseEntity<SongDTO> createSong(@RequestBody Song song) {
        SongDTO createdSong = songService.createSong(song);
        return ResponseEntity.ok(createdSong);
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    @Operation(summary = "Get all songs")
    @GetMapping
    public ResponseEntity<List<SongDTO>> getAllSongs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer time,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String album) {
        List<SongDTO> songs = songService.getAllSongs(title, time, url, album);
        return ResponseEntity.ok(songs);
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    @Operation(summary = "Get song by ID")
    @GetMapping("/{id}")
    public ResponseEntity<SongDTO> getSongById(@PathVariable Integer id) {
        SongDTO song = songService.getSongById(id);
        return ResponseEntity.ok(song);
    }

    @Operation(summary = "Get song by name")
    @GetMapping("/name/{name}")
    public ResponseEntity<SongDTO> getSongByName(@PathVariable String name) {
        SongDTO song = songService.getSongByName(name);
        return ResponseEntity.ok(song);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update song")
    @PutMapping("/{id}")
    public ResponseEntity<SongDTO> updateSong(@PathVariable Integer id, @RequestBody Song song) {
        SongDTO updatedSong = songService.updateSong(id, song);
        return ResponseEntity.ok(updatedSong);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete song")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable Integer id) {
        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    @Operation(summary = "Get songs by dynamic filter")
    @PostMapping("/filter")
    public ResponseEntity<PagedResponse<SongDTO>> filterSongs(@RequestBody FilterStruct filter) {
        PagedResponse<SongDTO> response = songService.filterSongs(filter);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Patch song")
    @PatchMapping("/{id}")
    public ResponseEntity<SongDTO> patchSong(@PathVariable Integer id, @RequestBody Song song) {
        SongDTO updatedSong = songService.patchSong(id, song);
        return ResponseEntity.ok(updatedSong);
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    @Operation(summary = "Get random song")
    @GetMapping("/random")
    public ResponseEntity<SongDTO> getRandomSong() {
        SongDTO song = songService.getRandomSong();
        return ResponseEntity.ok(song);
    }
}