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

import com.musicapp.musicstream.common.AlbumSpecification;
import com.musicapp.musicstream.common.HistoryVoid;
import com.musicapp.musicstream.dto.AlbumDTO;
import com.musicapp.musicstream.dto.DTOUtils;
import com.musicapp.musicstream.entities.Album;
import com.musicapp.musicstream.entities.Artist;
import com.musicapp.musicstream.entities.FilterStruct;
import com.musicapp.musicstream.entities.History;
import com.musicapp.musicstream.entities.Song;
import com.musicapp.musicstream.exception.ApiRuntimeException;
import com.musicapp.musicstream.repository.AlbumRepository;
import com.musicapp.musicstream.repository.ArtistRepository;
import com.musicapp.musicstream.repository.HistoryRepository;
import com.musicapp.musicstream.repository.SongRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin(origins = "http://localhost:4200")
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

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private HistoryVoid historyVoid;

    @Operation(summary = "Create a new album")
    @PostMapping
    public ResponseEntity<?> createAlbum(@RequestBody Album album) {
        
        // Si ya existe un álbum con el mismo nombre no se puede crear
        if (albumRepository.findByTitle(album.getTitle()) != null) {
            throw new ApiRuntimeException("This album name already exists " + album.getTitle(), 409);
        }

        if (album.getArtist() == null || artistRepository.findById(album.getArtist().getId()).isEmpty()) {
            // Retorna un error 412 si el artista no existe
            throw new ApiRuntimeException("Artist does not exist ", 412);
        }

        Set<Song> existingSongs = new HashSet<>();
        if (album.getSongs() != null && !album.getSongs().isEmpty()) {
            for (Song song : album.getSongs()) {
                // Comprobar que la canción existe
                Song existingSong = songRepository.findByTitle(song.getTitle());
                if (existingSong == null) {
                    //Si no existe creamos una cancion nueva
                    existingSong = new Song();
                    existingSong.setTitle(song.getTitle());
                    existingSong.setTime(song.getTime());
                    existingSong.setUrl(song.getUrl());
                    existingSong.setArtists(song.getArtists());
                    existingSong.setGenreList(song.getGenreList());
                    
                    
                }
                existingSongs.add(existingSong);
                songRepository.save(existingSong);
            }
        }

        album.setSongs(existingSongs);
        album.setNumberOfSongs(existingSongs.size());
        Album savedAlbum = albumRepository.save(album); // Guarda el álbum en la base de datos
        // Añadir la relación con las canciones
        for (Song song : existingSongs) {
            song.setAlbum(savedAlbum);
            Set<Artist> artists = new HashSet<>();
            Artist artist = artistRepository.findById(album.getArtist().getId()).get();
            artist.addSong(song);

            artists.add(artist);
            song.setArtists(artists);

            songRepository.save(song);
        }

        // Añadir el álbum al historial
        historyVoid.createEntry("Album", savedAlbum.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all albums")
    @GetMapping
    public ResponseEntity<List<AlbumDTO>> getAllAlbums(@RequestParam(required = false, name = "title") String title,
                                                       @RequestParam(required = false, name = "year") Integer year,
                                                       @RequestParam(required = false, name = "numberOfSongs") Integer numberOfSongs,
                                                       @RequestParam(required = false, name = "url") String url) {
        Specification<Album> spec = Specification.where(AlbumSpecification.hasTitle(title))
                                                    .and(AlbumSpecification.hasYear(year))
                                                    .and(AlbumSpecification.hasUrl(url))
                                                    .and(AlbumSpecification.hasNumberOfSongs(numberOfSongs));
        List<Album> artists = (List<Album>) albumRepository.findAll(spec);
        List<AlbumDTO> artistDTOs = artists.stream()
                                             .map(dtoUtil::convertToDto)
                                             .collect(Collectors.toList());
        return ResponseEntity.ok(artistDTOs);
    }

    @Operation(summary = "Get album by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AlbumDTO> getAlbumById(@PathVariable Integer id) {
        //Si no existe el album que devuelva un error 404
        if (!albumRepository.existsById(id)) {
            throw new ApiRuntimeException("Album not found with id " + id,404);
        }
        Optional<Album> album = albumRepository.findById(id);
        //Creamos el dto a partir del album
        AlbumDTO albumDTO = album.map(dtoUtil::convertToDto)
                                 .orElse(null);
        return albumDTO != null ? ResponseEntity.ok(albumDTO) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Get album by name")
    @GetMapping("/name/{name}")
    public ResponseEntity<AlbumDTO> getAlbumByName(@PathVariable String name) {
        if(albumRepository.findByTitle(name) == null){
            throw new ApiRuntimeException("Album not found with name " + name,404);
        }

        Album album = albumRepository.findByTitle(name);
        AlbumDTO albumDTO = dtoUtil.convertToDto(album);
        return album != null ? ResponseEntity.ok(albumDTO) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Update album")
    @PutMapping("/{id}")
    public ResponseEntity<AlbumDTO> updateAlbum(@PathVariable Integer id, @RequestBody AlbumDTO albumDetailsDTO) {
        if(albumRepository.findById(id).isEmpty()){
            throw new ApiRuntimeException("Album not found with id " + id,404);
        }
        if (albumRepository.findByTitle(albumDetailsDTO.getTitle()) != null) {
            throw new ApiRuntimeException("This album name already exists " + albumDetailsDTO.getTitle(), 409);
        }

        Album album = albumRepository.findById(id).get();
        album.setTitle(albumDetailsDTO.getTitle());
        album.setYear(albumDetailsDTO.getYear());
        album.setDescription(albumDetailsDTO.getDescription());
        album.setNumberOfSongs(albumDetailsDTO.getNumberOfSongs());
        album.setArtist(artistRepository.findById(albumDetailsDTO.getArtist()).orElse(null));

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
            throw new ApiRuntimeException("Album not found with id " + id,404);
        }
        //Por cada cancion, eliminar la relacion con el album
        Album album = albumRepository.findById(id).get();
        for (Song song : album.getSongs()) {
            song.setAlbum(null);
            songRepository.save(song);
        }

                //Eliminar el artista del historial
        Iterable<History> histories = historyRepository.findAll();
        for (History history : histories) {
            if (history.getType().equals("album") && history.getIdEntity().equals(id)) {
                historyRepository.delete(history);
            }
        }

        albumRepository.deleteById(id);
        historyVoid.deleteEntries("Album",id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get album by dynamic filter")
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
        Specification<Album> specification = AlbumSpecification.getAlbumsByFilters(struct.getListSearchCriteria());

        // Realizar la consulta con el repositorio utilizando Pageable y Specification
        Page<Album> albums = albumRepository.findAll(specification, pageable);

        // Convertir a DTO
        List<AlbumDTO> albumDTOs = albums.stream()
                                            .map(dtoUtil::convertToDto)
                                            .collect(Collectors.toList());

        return ResponseEntity.ok(albumDTOs);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AlbumDTO> patchAlbum(@PathVariable Integer id, @RequestBody Album albumDetails) {
        Optional<Album> albumOptional = albumRepository.findById(id);
        if (!albumOptional.isPresent()) {
            throw new ApiRuntimeException("Album not found with id : " + id,404);
        }

        Album album = albumOptional.get();
        if (albumDetails.getTitle() != null) {
            if(albumRepository.findByTitle(albumDetails.getTitle()) != null){
                if(!album.getTitle().equals(albumDetails.getTitle())){
                    throw new ApiRuntimeException("This album name already exists " + albumDetails.getTitle(), 409);
                }
            }
            album.setTitle(albumDetails.getTitle());
        }
        if (albumDetails.getYear() != null) {
            album.setYear(albumDetails.getYear());
        }
        if (albumDetails.getDescription() != null) {
            album.setDescription(albumDetails.getDescription());
        }
        if (albumDetails.getNumberOfSongs() != null) {
            album.setNumberOfSongs(albumDetails.getNumberOfSongs());
        }
        if (albumDetails.getArtist() != null) {
            Artist artist = artistRepository.findById(albumDetails.getArtist().getId()).orElse(null);
            album.setArtist(artist);
        }
        if (albumDetails.getUrl() != null) {
            album.setUrl(albumDetails.getUrl());
        }

        if( albumDetails.getSongs()!=null){
            //Limpiamos las canciones anteriores
            album.getSongs().clear();
            Set<Song> existingSongs = new HashSet<>();
            for (Song song : albumDetails.getSongs()) {
                // Comprobar que la canción existe
                Song existingSong = songRepository.findByTitle(song.getTitle());
                if (existingSong == null) {
                    //Si no existe creamos una cancion nueva
                    existingSong = new Song();
                    existingSong.setTitle(song.getTitle());
                    existingSong.setTime(song.getTime());
                    existingSong.setUrl(song.getUrl());
                    existingSong.setArtists(song.getArtists());
                    existingSong.setGenreList(song.getGenreList());
                }
                existingSongs.add(existingSong);
                songRepository.save(existingSong);
            }
            album.setSongs(existingSongs);
            album.setNumberOfSongs(existingSongs.size());
        }

        Album updatedAlbum = albumRepository.save(album);
        AlbumDTO albumDTO = dtoUtil.convertToDto(updatedAlbum);
        return ResponseEntity.ok(albumDTO);
    }
}
