package com.musicapp.musicstream.service.impl;

import com.musicapp.musicstream.common.HistoryVoid;
import com.musicapp.musicstream.common.SongSpecification;
import com.musicapp.musicstream.dto.DTOUtils;
import com.musicapp.musicstream.dto.PagedResponse;
import com.musicapp.musicstream.dto.SongDTO;
import com.musicapp.musicstream.entities.*;
import com.musicapp.musicstream.exception.ApiRuntimeException;
import com.musicapp.musicstream.repository.*;
import com.musicapp.musicstream.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;
    private final DTOUtils dtoUtils;
    private final HistoryVoid historyVoid;

    @Autowired
    public SongServiceImpl(SongRepository songRepository,
                         ArtistRepository artistRepository,
                         AlbumRepository albumRepository,
                         GenreRepository genreRepository,
                         DTOUtils dtoUtils,
                         HistoryVoid historyVoid) {
        this.songRepository = songRepository;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.genreRepository = genreRepository;
        this.dtoUtils = dtoUtils;
        this.historyVoid = historyVoid;
    }

    @Override
    public SongDTO createSong(Song song) {
        validateSongCreation(song);
        
        Song savedSong = saveSongWithRelations(song);
        historyVoid.createEntry("Song", savedSong.getId());
        
        return dtoUtils.convertToDto(savedSong);
    }

    private void validateSongCreation(Song song) {
        if (songRepository.findByTitle(song.getTitle()) != null) {
            throw new ApiRuntimeException("Song with title " + song.getTitle() + " already exists", 400);
        }
        if (song.getArtists() == null || song.getArtists().isEmpty()) {
            throw new ApiRuntimeException("Artists are required", 400);
        }
    }

    private Song saveSongWithRelations(Song song) {
        processAlbum(song);
        Set<Artist> artists = processArtists(song);
        Set<Genre> genres = processGenres(song);
        
        song.setArtists(artists);
        song.setGenreList(genres);
        
        Song savedSong = songRepository.save(song);
        
        updateArtistRelations(savedSong, artists);
        updateGenreRelations(savedSong, genres);
        
        return savedSong;
    }

    private void processAlbum(Song song) {
        if (song.getAlbum() != null) {
            Album album = albumRepository.findById(song.getAlbum().getId())
                    .orElseThrow(() -> new ApiRuntimeException("Album not found",404));
            album.setNumberOfSongs(album.getSongs().size() + 1);
            albumRepository.save(album);
            song.setAlbum(album);
        }
    }

    private Set<Artist> processArtists(Song song) {
        Set<Artist> artists = new HashSet<>();
        for (Artist artist : song.getArtists()) {
            Artist existingArtist = artistRepository.findById(artist.getId())
                    .orElseThrow(() -> new ApiRuntimeException("Artist not found",404));
            artists.add(existingArtist);
        }
        return artists;
    }

    private Set<Genre> processGenres(Song song) {
        Set<Genre> genres = new HashSet<>();
        if (song.getGenreList() != null) {
            for (Genre genre : song.getGenreList()) {
                Genre existingGenre = genreRepository.findById(genre.getId())
                        .orElseThrow(() -> new ApiRuntimeException("Genre not found",404));
                genres.add(existingGenre);
            }
        }
        return genres;
    }

    private void updateArtistRelations(Song song, Set<Artist> artists) {
        for (Artist artist : artists) {
            artist.addSong(song);
            artistRepository.save(artist);
        }
    }

    private void updateGenreRelations(Song song, Set<Genre> genres) {
        for (Genre genre : genres) {
            genre.addSong(song);
            genreRepository.save(genre);
        }
    }

    @Override
    public List<SongDTO> getAllSongs(String title, Integer time, String url, String album) {
        Integer albumId = getAlbumId(album);
        Specification<Song> spec = buildSongSpecification(title, time, url, albumId);
        
        List<Song> songs = songRepository.findAll(spec);
        return songs.stream()
                .map(dtoUtils::convertToDto)
                .collect(Collectors.toList());
    }

    private Integer getAlbumId(String albumTitle) {
        if (albumTitle != null) {
            Album album = albumRepository.findByTitle(albumTitle);
            if (album == null) {
                throw new ApiRuntimeException("Album not found", 404);
            }
            return album.getId();
        }
        return null;
    }

    private Specification<Song> buildSongSpecification(String title, Integer time, String url, Integer albumId) {
        return Specification.where(SongSpecification.hasTitle(title))
                .and(SongSpecification.hasTime(time))
                .and(SongSpecification.hasUrl(url))
                .and(SongSpecification.hasAlbum(albumId));
    }

    @Override
    public SongDTO getSongById(Integer id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ApiRuntimeException("Song not found",404));
        return dtoUtils.convertToDto(song);
    }

    @Override
    public SongDTO getSongByName(String name) {
        Song song = songRepository.findByTitle(name);
        if (song == null) {
            throw new ApiRuntimeException("Song not found",404);
        }
        return dtoUtils.convertToDto(song);
    }

    @Override
    public SongDTO updateSong(Integer id, Song songDetails) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ApiRuntimeException("Song not found",404));

        updateSongFields(song, songDetails);
        updateSongRelations(song, songDetails);

        Song updatedSong = songRepository.save(song);
        return dtoUtils.convertToDto(updatedSong);
    }

    private void updateSongFields(Song song, Song songDetails) {
        song.setTitle(songDetails.getTitle());
        song.setTime(songDetails.getTime());
        song.setUrl(songDetails.getUrl());
        
        if (songDetails.getAlbum() != null) {
            Album album = albumRepository.findById(songDetails.getAlbum().getId())
                    .orElseThrow(() -> new ApiRuntimeException("Album not found",404));
            song.setAlbum(album);
        }
    }

    private void updateSongRelations(Song song, Song songDetails) {
        if (songDetails.getArtists() != null) {
            updateArtistRelationsForSong(song, songDetails.getArtists());
        }
        if (songDetails.getGenreList() != null) {
            updateGenreRelationsForSong(song, songDetails.getGenreList());
        }
    }

    private void updateArtistRelationsForSong(Song song, Set<Artist> newArtists) {
        // Remove song from old artists
        song.getArtists().forEach(artist -> {
            artist.removeSong(song);
            artistRepository.save(artist);
        });

        // Add song to new artists
        Set<Artist> artists = new HashSet<>();
        for (Artist artist : newArtists) {
            Artist existingArtist = artistRepository.findById(artist.getId())
                    .orElseThrow(() -> new ApiRuntimeException("Artist not found",404));
            existingArtist.addSong(song);
            artistRepository.save(existingArtist);
            artists.add(existingArtist);
        }
        song.setArtists(artists);
    }

    private void updateGenreRelationsForSong(Song song, Set<Genre> newGenres) {
        // Remove song from old genres
        song.getGenreList().forEach(genre -> {
            genre.removeSong(song);
            genreRepository.save(genre);
        });

        // Add song to new genres
        Set<Genre> genres = new HashSet<>();
        for (Genre genre : newGenres) {
            Genre existingGenre = genreRepository.findById(genre.getId())
                    .orElseThrow(() -> new ApiRuntimeException("Genre not found",404));
            existingGenre.addSong(song);
            genreRepository.save(existingGenre);
            genres.add(existingGenre);
        }
        song.setGenreList(genres);
    }

    @Override
    public void deleteSong(Integer id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ApiRuntimeException("Song not found",404));

        removeSongRelations(song);
        songRepository.delete(song);
        historyVoid.deleteEntries("Song", id);
    }

    private void removeSongRelations(Song song) {
        // Remove from artists
        song.getArtists().forEach(artist -> {
            artist.removeSong(song);
            artistRepository.save(artist);
        });

        // Remove from genres
        song.getGenreList().forEach(genre -> {
            genre.removeSong(song);
            genreRepository.save(genre);
        });

        // Update album count if exists
        if (song.getAlbum() != null) {
            Album album = song.getAlbum();
            album.setNumberOfSongs(album.getSongs().size() - 1);
            albumRepository.save(album);
        }
    }

    @Override
    public PagedResponse<SongDTO> filterSongs(FilterStruct filter) {
        Sort sort = buildSort(filter.getListOrderCriteria());
        Pageable pageable = PageRequest.of(
                filter.getPage().getPageIndex(),
                filter.getPage().getPageSize(),
                sort);

        Specification<Song> specification = SongSpecification.getSongsByFilters(filter.getListSearchCriteria());
        Page<Song> songsPage = songRepository.findAll(specification, pageable);

        List<SongDTO> songDTOs = songsPage.stream()
                .map(dtoUtils::convertToDto)
                .collect(Collectors.toList());

        return buildPagedResponse(songsPage, songDTOs);
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

    private PagedResponse<SongDTO> buildPagedResponse(Page<Song> songsPage, List<SongDTO> songDTOs) {
        PagedResponse<SongDTO> response = new PagedResponse<>();
        response.setContent(songDTOs);
        response.setTotalElements(songsPage.getTotalElements());
        response.setTotalPages(songsPage.getTotalPages());
        response.setPageNumber(songsPage.getNumber());
        response.setPageSize(songsPage.getSize());
        return response;
    }

    @Override
    public SongDTO patchSong(Integer id, Song songDetails) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ApiRuntimeException("Song not found",404));

        patchSongFields(song, songDetails);
        patchSongRelations(song, songDetails);

        Song updatedSong = songRepository.save(song);
        return dtoUtils.convertToDto(updatedSong);
    }

    private void patchSongFields(Song song, Song songDetails) {
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
            Album album = albumRepository.findById(songDetails.getAlbum().getId())
                    .orElseThrow(() -> new ApiRuntimeException("Album not found",404));
            song.setAlbum(album);
        }
    }

    private void patchSongRelations(Song song, Song songDetails) {
        if (songDetails.getArtists() != null) {
            updateArtistRelationsForSong(song, songDetails.getArtists());
        }
        if (songDetails.getGenreList() != null) {
            updateGenreRelationsForSong(song, songDetails.getGenreList());
        }
    }

    @Override
    public SongDTO getRandomSong() {
        List<Song> songs = (List<Song>) songRepository.findAll();
        if (songs.isEmpty()) {
            throw new ApiRuntimeException("No songs available", 404);
        }
        int randomIndex = new Random().nextInt(songs.size());
        return dtoUtils.convertToDto(songs.get(randomIndex));
    }
}