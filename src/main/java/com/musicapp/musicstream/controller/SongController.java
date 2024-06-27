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

import com.musicapp.musicstream.dto.DTOUtils;
import com.musicapp.musicstream.dto.SongDTO;
import com.musicapp.musicstream.entities.Album;
import com.musicapp.musicstream.entities.Artist;
import com.musicapp.musicstream.entities.Song;
import com.musicapp.musicstream.repository.AlbumRepository;
import com.musicapp.musicstream.repository.ArtistRepository;
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
    private ArtistRepository artistRepository;

    @Autowired
    private AlbumRepository albumRepository;
    
    @Autowired
    private DTOUtils dtoUtil;

    @Operation(summary = "Create a new song")
    @PostMapping
    public ResponseEntity<?> createSong(@RequestBody Song song) {
        
        // Si ya existe una canción con el mismo nombre no se puede crear
        if (songRepository.findByTitle(song.getTitle()) != null) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("Ya existe una canción con el nombre " + song.getTitle());
        }

        if (song.getArtists() == null || song.getArtists().isEmpty()) {
            // Retorna un error 412 si no se especifica ningún artista
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se ha especificado ningún artista");
        }

        if (song.getAlbum() != null) {
            if (albumRepository.findById(song.getAlbum().getId()).isEmpty()) {
                // Retorna un error 412 si el álbum no existe
                return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("El álbum con ID " + song.getAlbum().getId() + " no existe");
            }
            else {
                Album album = albumRepository.findById(song.getAlbum().getId()).get();
                album.setNumberOfSongs(album.getSongs().size());
                albumRepository.save(album);
                song.setAlbum(album);
            }
        }

        Set<Artist> existingArtists = new HashSet<>();
        for (Artist artist : song.getArtists()) {
            // Comprobar que el artista existe
            Artist existingArtist = artistRepository.findById(artist.getId()).orElse(null);
            if (existingArtist == null) {
                return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("El artista con ID " + artist.getId() + " no existe");
            }
            existingArtists.add(existingArtist);
        }

        song.setArtists(existingArtists);

        Song savedSong = songRepository.save(song); // Guarda la canción en la base de datos

        // Añadir la relación con los artistas
        for (Artist artist : existingArtists) {
            artist.addSong(savedSong);
            artistRepository.save(artist);
        }
        //Creamos el dto
        SongDTO songDTO = dtoUtil.convertToDto(savedSong);
        songDTO.setId(savedSong.getId());
        return ResponseEntity.ok(songDTO);
    }

    @Operation(summary = "Get all songs")
    @GetMapping
    public ResponseEntity<List<SongDTO>> getAllSongs() {
        List<Song> songs = (List<Song>) songRepository.findAll();
        List<SongDTO> songsDTO = songs.stream()
                                             .map(dtoUtil::convertToDto)
                                             .collect(Collectors.toList());
        return ResponseEntity.ok(songsDTO);
    }

    @Operation(summary = "Get song by ID")
    @GetMapping("/{id}")
    public ResponseEntity<SongDTO> getSongById(@PathVariable Integer id) {
        Optional<Song> song = songRepository.findById(id);
        //Creamos el dto
        SongDTO songDTO = song.map(dtoUtil::convertToDto)
                                     .orElse(null);
        return songDTO != null ? ResponseEntity.ok(songDTO) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Get song by name")
    @GetMapping("/name/{name}")
    public ResponseEntity<SongDTO> getSongByName(@PathVariable String name) {
        Song song = songRepository.findByTitle(name);
        SongDTO songDTO = dtoUtil.convertToDto(song);
        return song != null ? ResponseEntity.ok(songDTO) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Update song")
    @PutMapping("/{id}")
    public ResponseEntity<SongDTO> updateSong(@PathVariable Integer id, @RequestBody Song songDetails) {
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
        SongDTO songDTO = dtoUtil.convertToDto(updatedSong);
        songDTO.setId(updatedSong.getId());
        return ResponseEntity.ok(songDTO);
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
