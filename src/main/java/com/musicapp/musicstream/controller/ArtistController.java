package com.musicapp.musicstream.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
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

import com.musicapp.musicstream.common.ArtistSpecification;
import com.musicapp.musicstream.dto.ArtistDTO;
import com.musicapp.musicstream.dto.DTOUtils;
import com.musicapp.musicstream.entities.Album;
import com.musicapp.musicstream.entities.Artist;
import com.musicapp.musicstream.entities.FilterStruct;
import com.musicapp.musicstream.entities.Song;
import com.musicapp.musicstream.exception.ApiRuntimeException;
import com.musicapp.musicstream.repository.AlbumRepository;
import com.musicapp.musicstream.repository.ArtistRepository;
import com.musicapp.musicstream.repository.SongRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/artists")
@Tag(name = "Artist", description = "Operations related to Artists")
public class ArtistController {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private DTOUtils dtoUtil;

    @Operation(summary = "Create a new artist")
    @PostMapping
    public ResponseEntity<ArtistDTO> createArtist(@RequestBody ArtistDTO artistdto) 
{   //Comprobamos que no exista ese artista y validamos los campos
        if (artistRepository.findByName(artistdto.getName()) != null) {
            throw new ApiRuntimeException("Artist already exists: " + artistdto.getName(), 409);
        }
        if(artistdto.getName()==null || artistdto.getAge()==0 || artistdto.getDateOfBirth()==null){
            throw new ApiRuntimeException("Artist fields are not valid" + artistdto.getName() + artistdto.getAge() + artistdto.getDateOfBirth() , 412);
        }    
    //Creamos el artista a partir del dto
        Artist artist = new Artist();
        artist.setName(artistdto.getName());
        artist.setAge(artistdto.getAge());
        artist.setCountry(artistdto.getCountry());
        artist.setDateOfBirth(artistdto.getDateOfBirth());
        Artist savedArtist = artistRepository.save(artist);
        //artistdto.setId(savedArtist.getId());
        return ResponseEntity.ok(artistdto);
    }

    @Operation(summary = "Get all artists")
    @GetMapping
    public ResponseEntity<List<ArtistDTO>> getAllArtists(
            @RequestParam(required = false, name = "name") String name,
            @RequestParam(required = false, name = "country") String country,
            @RequestParam(required = false, name = "age") Integer age,
            @RequestParam(required = false, name = "dateOfBirth") String dateOfBirth) {

        // Construir la especificación combinando las condiciones basadas en los parámetros recibidos
        Specification<Artist> spec = Specification.where(ArtistSpecification.hasName(name))
                .and(ArtistSpecification.hasCountry(country))
                .and(ArtistSpecification.hasAge(age))
                .and(ArtistSpecification.hasDateOfBirth(dateOfBirth));

        // Obtener la lista de artistas según la especificación
        List<Artist> artists = artistRepository.findAll(spec);

        // Convertir los artistas a DTOs
        List<ArtistDTO> artistDTOs = artists.stream()
                .map(dtoUtil::convertToDto)
                .collect(Collectors.toList());

        // Devolver la respuesta HTTP con la lista de DTOs de artistas
        return ResponseEntity.ok(artistDTOs);
    }

    @Operation(summary = "Get artist by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ArtistDTO> getArtistById(@PathVariable Integer id) {
        if (!artistRepository.existsById(id)) {
            throw new ApiRuntimeException("Artist not found with id : " + id,404);
        }
        Optional<Artist> artist = artistRepository.findById(id);
        //Creamos el dto a partir del artista
        ArtistDTO artistDTO = artist.map(dtoUtil::convertToDto)
                                    .orElse(null);
        return artistDTO != null ? ResponseEntity.ok(artistDTO) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Get artist by name")
    @GetMapping("/name/{name}")
    public ResponseEntity<ArtistDTO> getArtistByName(@PathVariable String name) {
        Artist artist = artistRepository.findByName(name);
        ArtistDTO artistDTO = dtoUtil.convertToDto(artist);
        if (artist == null) {
            throw new ApiRuntimeException("Artist not found with name : " + name,404);
        }
        return ResponseEntity.ok(artistDTO);
    }

    @Operation(summary = "Update artist")
    @PutMapping("/{id}")
    public ResponseEntity<ArtistDTO> updateArtist(@PathVariable Integer id, @RequestBody ArtistDTO artistDetailsDTO) {
        if (!artistRepository.existsById(id)) {
            throw new ApiRuntimeException("Artist not found with id : " + id,404);
        }
        Optional<Artist> artistOptional = artistRepository.findById(id);
        if (!artistOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Artist artist = artistOptional.get();
        artist.setName(artistDetailsDTO.getName());
        artist.setAge(artistDetailsDTO.getAge());
        artist.setCountry(artistDetailsDTO.getCountry());
        artist.setDateOfBirth(artistDetailsDTO.getDateOfBirth());
        Artist updatedArtist = artistRepository.save(artist);
        //artistDetailsDTO.setId(updatedArtist.getId());
        return ResponseEntity.ok(artistDetailsDTO);
    }

    @Operation(summary = "Delete artist")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArtist(@PathVariable Integer id) {
        if (!artistRepository.existsById(id)) {
            throw new ApiRuntimeException("Artist not found with id : " + id,404);
        }

        artistRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

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
        Specification<Artist> specification = ArtistSpecification.getArtistsByFilters(struct.getListSearchCriteria());

        // Realizar la consulta con el repositorio utilizando Pageable y Specification
        Page<Artist> artists = artistRepository.findAll(specification, pageable);

        // Convertir a DTO
        List<ArtistDTO> artistDTOs = artists.stream()
                                            .map(dtoUtil::convertToDto)
                                            .collect(Collectors.toList());

        return ResponseEntity.ok(artistDTOs);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ArtistDTO> patchArtist(@PathVariable Integer id, @RequestBody Artist artistDetails) {
        if (!artistRepository.existsById(id)) {
            throw new ApiRuntimeException("Artist not found with id : " + id,404);
        }
        Optional<Artist> artistOptional = artistRepository.findById(id);
        if (!artistOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Artist artist = artistOptional.get();
        if (artistDetails.getName() != null) {
            artist.setName(artistDetails.getName());
        }

        if (artistDetails.getAge() != 0) {
            artist.setAge(artistDetails.getAge());
        }
        
        if (artistDetails.getCountry() != null) {
            artist.setCountry(artistDetails.getCountry());
        }
        if (artistDetails.getDateOfBirth() != null) {
            artist.setDateOfBirth(artistDetails.getDateOfBirth());
        }

        if(artistDetails.getSingleSongList()!=null) {
            //Limpiamos las canciones anteriores
            artist.getSingleSongList().clear();
            for ( Song song : artistDetails.getSingleSongList()) {
                artist.addSong(songRepository.findById(song.getId()).get());
            }
        }

        if(artistDetails.getAlbums()!=null) {
            //Limpiamos los álbumes anteriores
            artist.getAlbums().clear();
            for ( Album album : artistDetails.getAlbums()) {
                artist.addAlbum(albumRepository.findById(album.getId()).get());
            }
        }

        Artist updatedArtist = artistRepository.save(artist);
        ArtistDTO artistDetailsDTO = dtoUtil.convertToDto(updatedArtist);
        return ResponseEntity.ok(artistDetailsDTO);
        
    }
    
    
}

