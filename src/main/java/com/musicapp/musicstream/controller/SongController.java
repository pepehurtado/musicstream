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

import com.musicapp.musicstream.entities.Artist;
import com.musicapp.musicstream.entities.Song;
import com.musicapp.musicstream.repository.SongRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/songs")
@Tag(name = "Song", description = "Operations related to Song")
public class SongController {

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private ArtistController artistController;

    @Operation(summary = "Create a new song")
    @PostMapping
    public ResponseEntity<Song> createSong(@RequestBody Song song) {
        if(song.getArtists() == null || song.getArtists().isEmpty()) {
            // Retorna un error 412 si no se especifica ningún artista
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        else{
            for (Artist artist : song.getArtists()) {
                //Si el artista no existe, retorna un error 412. En caso contrario lo añade a  su lista de canciones
                if(artistController.getArtistById(artist.getId()).getStatusCodeValue() == 404){
                    return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
                }
                else{
                    artistController.getArtistByName(artist.getName()).getBody().getSingleSongList().add(song);
                }
            }   
        }
        
        Song savedSong = songRepository.save(song); // Guarda la canción en la base de datos

        return ResponseEntity.ok(savedSong);
    }

    @Operation(summary = "Get all songs")
    @GetMapping
    public ResponseEntity<Iterable<Song>> getAllSongs() {
        Iterable<Song> artists = songRepository.findAll();
        return ResponseEntity.ok(artists);
    }

    @Operation(summary = "Get song by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Song> getSongById(@PathVariable Integer id) {
        Optional<Song> song = songRepository.findById(id);
        return song.map(ResponseEntity::ok)
                     .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get song by name")
    @GetMapping("/name/{name}")
    public ResponseEntity<Song> getSongByName(@PathVariable String name) {
        Song song = songRepository.findByTitle(name);
        return song != null ? ResponseEntity.ok(song) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Update song")
    @PutMapping("/{id}")
    public ResponseEntity<Song> updateSong(@PathVariable Integer id, @RequestBody Song songDetails) {
        Optional<Song> songOptional = songRepository.findById(id);
        if (!songOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Song song = songOptional.get();
        song.setTitle(songDetails.getTitle());
        song.setTime(songDetails.getTime());
        //song.setAlbumId(songDetails.getAlbumId());
        song.setUrl(songDetails.getUrl());

        Song updatedSong = songRepository.save(song);
        return ResponseEntity.ok(updatedSong);
    }

    @Operation(summary = "Delete song")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable Integer id) {
        if (!songRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        songRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
