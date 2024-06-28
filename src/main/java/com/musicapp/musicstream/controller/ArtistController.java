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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.musicapp.musicstream.common.ArtistSpecification;
import com.musicapp.musicstream.dto.ArtistDTO;
import com.musicapp.musicstream.dto.DTOUtils;
import com.musicapp.musicstream.entities.Artist;
import com.musicapp.musicstream.entities.FilterStruct;
import com.musicapp.musicstream.repository.ArtistRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/artists")
@Tag(name = "Artist", description = "Operations related to Artists")
public class ArtistController {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private DTOUtils dtoUtil;

    @Operation(summary = "Create a new artist")
    @PostMapping
    public ResponseEntity<ArtistDTO> createArtist(@RequestBody ArtistDTO artistdto) 
{       //Creamos el artista a partir del dto
        Artist artist = new Artist();
        artist.setName(artistdto.getName());
        artist.setAge(artistdto.getAge());
        artist.setCountry(artistdto.getCountry());
        artist.setDateOfBirth(artistdto.getDateOfBirth());
        Artist savedArtist = artistRepository.save(artist);
        artistdto.setId(savedArtist.getId());
        return ResponseEntity.ok(artistdto);
    }

    @Operation(summary = "Get all artists")
    @GetMapping
    public ResponseEntity<List<ArtistDTO>> getAllArtists() {
        List<Artist> artists = (List<Artist>) artistRepository.findAll();
        List<ArtistDTO> artistDTOs = artists.stream()
                                             .map(dtoUtil::convertToDto)
                                             .collect(Collectors.toList());
        return ResponseEntity.ok(artistDTOs);
    }

    @Operation(summary = "Get artist by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ArtistDTO> getArtistById(@PathVariable Integer id) {
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
        return artist != null ? ResponseEntity.ok(artistDTO) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Update artist")
    @PutMapping("/{id}")
    public ResponseEntity<ArtistDTO> updateArtist(@PathVariable Integer id, @RequestBody ArtistDTO artistDetailsDTO) {
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
        artistDetailsDTO.setId(updatedArtist.getId());
        return ResponseEntity.ok(artistDetailsDTO);
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
    
}
