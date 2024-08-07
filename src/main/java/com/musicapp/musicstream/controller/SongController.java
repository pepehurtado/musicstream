package com.musicapp.musicstream.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.musicapp.musicstream.common.HistoryVoid;
import com.musicapp.musicstream.common.SongSpecification;
import com.musicapp.musicstream.dto.DTOUtils;
import com.musicapp.musicstream.dto.SongDTO;
import com.musicapp.musicstream.entities.Album;
import com.musicapp.musicstream.entities.Artist;
import com.musicapp.musicstream.entities.FilterStruct;
import com.musicapp.musicstream.entities.Genre;
import com.musicapp.musicstream.entities.Song;
import com.musicapp.musicstream.exception.ApiRuntimeException;
import com.musicapp.musicstream.repository.AlbumRepository;
import com.musicapp.musicstream.repository.ArtistRepository;
import com.musicapp.musicstream.repository.GenreRepository;
import com.musicapp.musicstream.repository.SongRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
@CrossOrigin(origins = "http://localhost:4200")
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
    private GenreRepository genreRepository;
    
    @Autowired
    private DTOUtils dtoUtil;

    @Autowired
    private HistoryVoid historyVoid;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new song")
    @PostMapping
    public ResponseEntity<?> createSong(@RequestBody Song song) {
        
        // Si ya existe una canción con el mismo nombre no se puede crear
        if (songRepository.findByTitle(song.getTitle()) != null) {
            throw new ApiRuntimeException("This song name already exists " + song.getTitle(), 409);
            
        }
    
        if (song.getArtists() == null || song.getArtists().isEmpty()) {
            throw new ApiRuntimeException("Artist/s are required ", 400);
        }
    
        if (song.getAlbum() != null) {
            if (albumRepository.findById(song.getAlbum().getId()).isEmpty()) {
                // Retorna un error 412 si el álbum no existe
                throw new ApiRuntimeException("The album with ID " + song.getAlbum().getId() + " does not exist ", 412);
            } else {
                Album album = albumRepository.findById(song.getAlbum().getId()).get();
                album.setNumberOfSongs(album.getSongs().size() + 1);
                albumRepository.save(album);
                song.setAlbum(album);
            }
        }
    
        Set<Artist> existingArtists = new HashSet<>();
        for (Artist artist : song.getArtists()) {
            // Comprobar que el artista existe
            Artist existingArtist = artistRepository.findById(artist.getId()).orElse(null);
            if (existingArtist == null) {
                throw new ApiRuntimeException("The artist with ID " + artist.getId() + " does not exist", 412);
            }
            existingArtists.add(existingArtist);
        }
        song.setArtists(existingArtists);
    
        Set<Genre> existingGenres = new HashSet<>();
        for (Genre genre : song.getGenreList()) {
            Genre existingGenre = genreRepository.findById(genre.getId()).orElse(null);
            if (existingGenre == null) {
                throw new ApiRuntimeException("The genre with ID " + genre.getId() + " does not exist", 412);}
            existingGenres.add(existingGenre);
        }
        song.setGenreList(existingGenres);
    
        Song savedSong = songRepository.save(song); // Guarda la canción en la base de datos
    
        // Añadir la relación con los artistas
        for (Artist artist : existingArtists) {
            artist.addSong(savedSong);
            artistRepository.save(artist);
        }
        // Añadir la relación con los géneros
        for (Genre genre : existingGenres) {
            genre.addSong(savedSong);
            genreRepository.save(genre);
        }
        
        historyVoid.createEntry("Song", savedSong.getId());
        // Creamos el DTO
        SongDTO songDTO = dtoUtil.convertToDto(savedSong);
        songDTO.setId(savedSong.getId());
        return ResponseEntity.ok(songDTO);
    }
    
    @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    @Operation(summary = "Get all songs")
    @GetMapping
    public ResponseEntity<List<SongDTO>> getAllSongs(@RequestParam(required = false) String title,
                                                     @RequestParam(required = false) Integer time,
                                                     @RequestParam(required = false) String url,
                                                     @RequestParam(required = false) String album){
                                                        //Get the album id or null
                                                        Integer albumId = null;
                                                        if(album != null){
                                                            if(albumRepository.findByTitle(album) == null){
                                                                throw new ApiRuntimeException("Album not found with title : " + album, 404);
                                                            }
                                                            else{ albumId = albumRepository.findByTitle(album).getId();
                                                            }
                                                        }
                                                                
    Specification<Song> spec = Specification.where(SongSpecification.hasTitle(title))
                                                  .and(SongSpecification.hasTime(time))
                                                  .and(SongSpecification.hasUrl(url))
                                                  .and(SongSpecification.hasAlbum(albumId));

        List<Song> songs = (List<Song>) songRepository.findAll(spec);
        List<SongDTO> songsDTO = songs.stream()
                                             .map(dtoUtil::convertToDto)
                                             .collect(Collectors.toList());
        
        return ResponseEntity.ok(songsDTO);
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    @Operation(summary = "Get song by ID")
    @GetMapping("/{id}")
    public ResponseEntity<SongDTO> getSongById(@PathVariable Integer id) {
        if (!songRepository.existsById(id)) {
            throw new ApiRuntimeException("Song not found with id : " + id, 404);
        }
        Optional<Song> song = songRepository.findById(id);
        //Creamos el dto
        SongDTO songDTO = song.map(dtoUtil::convertToDto)
                                     .orElse(null);
        return songDTO != null ? ResponseEntity.ok(songDTO) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Get song by name")
    @GetMapping("/name/{name}")
    public ResponseEntity<SongDTO> getSongByName(@PathVariable String name) {
        if(songRepository.findByTitle(name) == null){
            throw new ApiRuntimeException("Song not found with name : " + name, 404);
        }
        Song song = songRepository.findByTitle(name);
        SongDTO songDTO = dtoUtil.convertToDto(song);
        return ResponseEntity.ok(songDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update song")
    @PutMapping("/{id}")
    public ResponseEntity<SongDTO> updateSong(@PathVariable Integer id, @RequestBody Song songDetails) {
        if (!songRepository.existsById(id)) {
            throw new ApiRuntimeException("Song not found with id : " + id, 404);
        }
        Song song = songRepository.findById(id).get();
        song.setTitle(songDetails.getTitle());
        song.setTime(songDetails.getTime());
        song.setAlbum(songDetails.getAlbum());
        song.setUrl(songDetails.getUrl());
        //Actualizar la relacion desde el lado del artista
        for (Artist artist : song.getArtists()) {
            Artist artistaforid = artistRepository.findById(artist.getId()).orElse(null);
            artistaforid.removeSong(song);
            artistRepository.save(artistaforid);
        }
        for(Artist artist : songDetails.getArtists()){
            Artist artistaforid = artistRepository.findById(artist.getId()).orElse(null);
            artistaforid.addSong(song);
            artistRepository.save(artistaforid);
        }
        song.setArtists(songDetails.getArtists());

        Song updatedSong = songRepository.save(song);
        SongDTO songDTO = dtoUtil.convertToDto(updatedSong);
        songDTO.setId(updatedSong.getId());
        return ResponseEntity.ok(songDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete song")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable Integer id) {
        if (!songRepository.existsById(id)) {
            throw new ApiRuntimeException("Song not found with id : " + id, 404);
        }
        //Eliminar la relacion con los artistas
        Song song = songRepository.findById(id).get();
        for (Artist artist : song.getArtists()) {
            Artist artistaforid = artistRepository.findById(artist.getId()).orElse(null);
            artistaforid.removeSong(song);
            artistRepository.save(artistaforid);
        }
        
        songRepository.deleteById(id);
        historyVoid.deleteEntries("Song", id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('USER')")
    @Operation(summary = "Get artist by dynamic filter")
    @PostMapping("/filter")
    public ResponseEntity<?> filterBy(@RequestBody FilterStruct struct) {

        // Construir el objeto Sort a partir de los criterios de ordenación
        Sort sort = Sort.unsorted();
        for (FilterStruct.SortCriteria sortCriteria : struct.getListOrderCriteria()) {
            Sort.Direction direction = sortCriteria.getValuesorOrder() == FilterStruct.SortValue.ASC ? 
                                       Sort.Direction.ASC : Sort.Direction.DESC;
            sort = sort.and(Sort.by(direction, sortCriteria.getSortBy()));
        }

        // Construir el objeto Pageable a partir de la información de paginación y ordenación
        Pageable pageable = PageRequest.of(struct.getPage().getPageIndex(), struct.getPage().getPageSize(), sort);

        // Construir la especificación a partir de los criterios de búsqueda
        Specification<Song> specification = SongSpecification.getSongsByFilters(struct.getListSearchCriteria());

        // Realizar la consulta con el repositorio utilizando Pageable y Specification
        Page<Song> songs = songRepository.findAll(specification, pageable);

        // Convertir a DTO
        List<SongDTO> songDTOs = songs.stream()
                                            .map(dtoUtil::convertToDto)
                                            .collect(Collectors.toList());

        return ResponseEntity.ok(songDTOs);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<SongDTO> patchSong(@PathVariable Integer id, @RequestBody Song songDetails) {
        
        if (!songRepository.existsById(id)) {
            throw new ApiRuntimeException("Song not found with id : " + id, 404);
        }
        Song song = songRepository.findById(id).get();
        if (songDetails.getTitle() != null) {
            song.setTitle(songDetails.getTitle());
        }
        if (songDetails.getTime() != null) {
            song.setTime(songDetails.getTime());
        }
        if (songDetails.getUrl() != null) {
            song.setUrl(songDetails.getUrl());
        }
        if (songDetails.getAlbum() != null) {
            Album album = albumRepository.findById(songDetails.getAlbum().getId()).get();
            song.setAlbum(album);
        }
        if (songDetails.getArtists() != null) {
            Set<Artist> artists = new HashSet<>();
            song.getArtists().forEach(artist -> artist.removeSong(song));
            for (Artist artist : songDetails.getArtists()) {
                Artist artisttoadd = artistRepository.findById(artist.getId()).get();
                artisttoadd.addSong(song);
                artists.add(artisttoadd);
            }
            song.setArtists(artists);
        }

        if (songDetails.getGenreList() != null) {
            Set<Genre> genres = new HashSet<>();
            song.getGenreList().forEach(genre -> genre.removeSong(song));
            for (Genre genre : songDetails.getGenreList()) {
                Genre genretoadd = genreRepository.findById(genre.getId()).get();
                genretoadd.addSong(song);
                genres.add(genre);
            }
            song.setGenreList(genres);
        }

        Song updatedSong = songRepository.save(song);
        SongDTO songDTO = dtoUtil.convertToDto(updatedSong);
        return ResponseEntity.ok(songDTO);
    }
}
