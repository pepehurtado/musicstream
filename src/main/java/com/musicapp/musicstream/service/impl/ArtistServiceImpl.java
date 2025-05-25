package com.musicapp.musicstream.service.impl;

import com.musicapp.musicstream.common.ArtistSpecification;
import com.musicapp.musicstream.common.HistoryVoid;
import com.musicapp.musicstream.dto.ArtistDTO;
import com.musicapp.musicstream.dto.DTOUtils;
import com.musicapp.musicstream.entities.*;
import com.musicapp.musicstream.exception.ApiRuntimeException;
import com.musicapp.musicstream.repository.AlbumRepository;
import com.musicapp.musicstream.repository.ArtistRepository;
import com.musicapp.musicstream.repository.SongRepository;
import com.musicapp.musicstream.service.ArtistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final DTOUtils dtoUtils;
    private final HistoryVoid historyVoid;

    @Autowired
    public ArtistServiceImpl(ArtistRepository artistRepository,
                           SongRepository songRepository,
                           AlbumRepository albumRepository,
                           DTOUtils dtoUtils,
                           HistoryVoid historyVoid) {
        this.artistRepository = artistRepository;
        this.songRepository = songRepository;
        this.albumRepository = albumRepository;
        this.dtoUtils = dtoUtils;
        this.historyVoid = historyVoid;
    }

    @Override
    public ArtistDTO createArtist(Artist artistToCreate) {
        if (artistRepository.findByName(artistToCreate.getName()) != null) {
            throw new ApiRuntimeException("Artist already exists " + artistToCreate.getName(), 409);
        }
        if (artistToCreate.getName() == null || artistToCreate.getAge() == 0 || artistToCreate.getDateOfBirth() == null) {
            throw new ApiRuntimeException("Artist fields are not valid", 412);
        }

        Artist artist = new Artist();
        artist.setName(artistToCreate.getName());
        artist.setAge(artistToCreate.getAge());
        artist.setCountry(artistToCreate.getCountry());
        artist.setDateOfBirth(artistToCreate.getDateOfBirth());

        Artist savedArtist = artistRepository.save(artist);
        Set<Artist> artists = new HashSet<>();
        artists.add(savedArtist);

        processSongs(artistToCreate, savedArtist, artists);
        processAlbums(artistToCreate, savedArtist);

        savedArtist = artistRepository.save(savedArtist);
        historyVoid.createEntry("Artist", savedArtist.getId());

        return dtoUtils.convertToDto(savedArtist);
    }

    private void processSongs(Artist artistToCreate, Artist savedArtist, Set<Artist> artists) {
        if (artistToCreate.getSingleSongList() != null) {
            for (Song song : artistToCreate.getSingleSongList()) {
                Song existingSong = songRepository.findById(song.getId()).orElse(null);
                if (existingSong == null) {
                    if (song.getTime() == null) {
                        throw new ApiRuntimeException("Can't create song " + song.getTitle() + " because time is null", 412);
                    }
                    song.setId(null);
                    song.setArtists(artists);
                    songRepository.save(song);
                    savedArtist.addSong(song);
                } else {
                    existingSong.getArtists().add(savedArtist);
                    songRepository.save(existingSong);
                }
            }
        }
    }

    private void processAlbums(Artist artistToCreate, Artist savedArtist) {
        if (artistToCreate.getAlbums() != null) {
            for (Album album : artistToCreate.getAlbums()) {
                Album existingAlbum = albumRepository.findByTitle(album.getTitle());
                if (existingAlbum == null) {
                    if (album.getYear() == null) {
                        throw new ApiRuntimeException("Can't create album " + album.getTitle() + " because year is null", 412);
                    }
                    album.setId(null);
                    album.setArtist(savedArtist);
                    existingAlbum = albumRepository.save(album);
                }
                savedArtist.addAlbum(existingAlbum);
                processAlbumSongs(album, existingAlbum, savedArtist);
            }
        }
    }

    private void processAlbumSongs(Album album, Album existingAlbum, Artist savedArtist) {
        if (album.getSongs() != null) {
            for (Song song : album.getSongs()) {
                Song existingSong = songRepository.findByTitle(song.getTitle());
                if (existingSong == null) {
                    if (song.getTime() == null) {
                        throw new ApiRuntimeException("Can't create song " + song.getTitle() + " because time is null", 412);
                    }
                    song.setAlbum(existingAlbum);
                    existingSong = songRepository.save(song);
                } else {
                    existingSong.setAlbum(existingAlbum);
                }
                savedArtist.addSong(existingSong);
                existingAlbum.addSong(existingSong);
                existingAlbum.setNumberOfSongs(existingAlbum.getSongs().size());
                albumRepository.save(existingAlbum);
            }
        }
    }

    @Override
    public List<ArtistDTO> getAllArtists(String name, String country, Integer age, String dateOfBirth) {
        Specification<Artist> spec = Specification.where(ArtistSpecification.hasName(name))
                .and(ArtistSpecification.hasCountry(country))
                .and(ArtistSpecification.hasAge(age))
                .and(ArtistSpecification.hasDateOfBirth(dateOfBirth));

        List<Artist> artists = artistRepository.findAll(spec);
        return artists.stream()
                .map(dtoUtils::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ArtistDTO getArtistById(Integer id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ApiRuntimeException("Artist not found with id " + id, 404));
        return dtoUtils.convertToDto(artist);
    }

    @Override
    public ArtistDTO getArtistByName(String name) {
        Artist artist = artistRepository.findByName(name);
        if (artist == null) {
            throw new ApiRuntimeException("Artist not found with name : " + name, 404);
        }
        return dtoUtils.convertToDto(artist);
    }

    @Override
    public ArtistDTO updateArtist(Integer id, ArtistDTO artistDetailsDTO) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ApiRuntimeException("Artist not found with id " + id, 404));

        artist.setName(artistDetailsDTO.getName());
        artist.setAge(artistDetailsDTO.getAge());
        artist.setCountry(artistDetailsDTO.getCountry());
        artist.setDateOfBirth(artistDetailsDTO.getDateOfBirth());

        Artist updatedArtist = artistRepository.save(artist);
        return dtoUtils.convertToDto(updatedArtist);
    }

    @Override
    public void deleteArtist(Integer id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ApiRuntimeException("Artist not found with id " + id, 404));

        // Eliminar asociaciones con canciones
        artist.getSingleSongList().forEach(song -> {
            song.getArtists().remove(artist);
            songRepository.save(song);
        });

        // Eliminar asociaciones con álbumes
        artist.getAlbums().forEach(album -> {
            album.setArtist(null);
            albumRepository.save(album);
        });

        artistRepository.delete(artist);
        historyVoid.deleteEntries("Artist", id);
    }

    @Override
    public Page<ArtistDTO> filterArtists(FilterStruct struct) {
        Sort sort = buildSort(struct.getListOrderCriteria());
        Pageable pageable = PageRequest.of(struct.getPage().getPageIndex(), struct.getPage().getPageSize(), sort);
    
        Page<Artist> artists;
    
        if (struct.getListSearchCriteria() == null) {
            artists = artistRepository.findAll(pageable);
        } else {
            Specification<Artist> specification = ArtistSpecification.getArtistsByFilters(struct.getListSearchCriteria());
            artists = artistRepository.findAll(specification, pageable);
        }
    
        // Mapear Page<Artist> a Page<ArtistDTO>
        return artists.map(dtoUtils::convertToDto);
    }
    

    @Override
    public ArtistDTO patchArtist(Integer id, Artist artistDetails) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ApiRuntimeException("Artist not found with id " + id, 404));

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

        if (artistDetails.getSingleSongList() != null) {
            artist.getSingleSongList().clear();
            artistDetails.getSingleSongList().forEach(song -> 
                artist.addSong(songRepository.findById(song.getId())
                    .orElseThrow(() -> new ApiRuntimeException("Song not found with id " + song.getId(), 404)))
            );
        }

        if (artistDetails.getAlbums() != null) {
            artist.getAlbums().clear();
            artistDetails.getAlbums().forEach(album -> 
                artist.addAlbum(albumRepository.findById(album.getId())
                    .orElseThrow(() -> new ApiRuntimeException("Album not found with id " + album.getId(), 404)))
            );
        }

        Artist updatedArtist = artistRepository.save(artist);
        return dtoUtils.convertToDto(updatedArtist);
    }

    private Sort buildSort(List<FilterStruct.SortCriteria> sortCriteria) {
        Sort sort = Sort.unsorted();
        if (sortCriteria != null) {
            for (FilterStruct.SortCriteria criteria : sortCriteria) {
                Sort.Direction direction = criteria.getValuesorOrder() == FilterStruct.SortValue.ASC ? 
                        Sort.Direction.ASC : Sort.Direction.DESC;
                sort = sort.and(Sort.by(direction, criteria.getSortBy()));
            }
        }
        return sort;
    }
}