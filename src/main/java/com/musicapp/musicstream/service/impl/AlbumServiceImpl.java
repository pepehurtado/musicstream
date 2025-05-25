package com.musicapp.musicstream.service.impl;

import com.musicapp.musicstream.common.AlbumSpecification;
import com.musicapp.musicstream.common.HistoryVoid;
import com.musicapp.musicstream.dto.AlbumDTO;
import com.musicapp.musicstream.dto.DTOUtils;
import com.musicapp.musicstream.entities.*;
import com.musicapp.musicstream.exception.ApiRuntimeException;
import com.musicapp.musicstream.repository.*;
import com.musicapp.musicstream.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlbumServiceImpl implements AlbumService {

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

    @Override
    public void createAlbum(Album album) {
        if (albumRepository.findByTitle(album.getTitle()) != null) {
            throw new ApiRuntimeException("This album name already exists " + album.getTitle(), 409);
        }

        if (album.getArtist() == null || artistRepository.findById(album.getArtist().getId()).isEmpty()) {
            throw new ApiRuntimeException("Artist does not exist ", 412);
        }

        Set<Song> existingSongs = new HashSet<>();
        if (album.getSongs() != null && !album.getSongs().isEmpty()) {
            for (Song song : album.getSongs()) {
                Song existingSong = songRepository.findByTitle(song.getTitle());
                if (existingSong == null) {
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
        Album savedAlbum = albumRepository.save(album);
        
        for (Song song : existingSongs) {
            song.setAlbum(savedAlbum);
            Set<Artist> artists = new HashSet<>();
            Artist artist = artistRepository.findById(album.getArtist().getId()).get();
            artist.addSong(song);
            artists.add(artist);
            song.setArtists(artists);
            songRepository.save(song);
        }

        historyVoid.createEntry("Album", savedAlbum.getId());
    }

    @Override
    public List<AlbumDTO> getAllAlbums(String title, Integer year, Integer numberOfSongs, String url) {
        Specification<Album> spec = Specification.where(AlbumSpecification.hasTitle(title))
                .and(AlbumSpecification.hasYear(year))
                .and(AlbumSpecification.hasUrl(url))
                .and(AlbumSpecification.hasNumberOfSongs(numberOfSongs));
        List<Album> albums = albumRepository.findAll(spec);
        return albums.stream()
                .map(dtoUtil::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public AlbumDTO getAlbumById(Integer id) {
        if (!albumRepository.existsById(id)) {
            throw new ApiRuntimeException("Album not found with id " + id, 404);
        }
        return albumRepository.findById(id)
                .map(dtoUtil::convertToDto)
                .orElse(null);
    }

    @Override
    public AlbumDTO getAlbumByName(String name) {
        Album album = albumRepository.findByTitle(name);
        if (album == null) {
            throw new ApiRuntimeException("Album not found with name " + name, 404);
        }
        return dtoUtil.convertToDto(album);
    }

    @Override
    public AlbumDTO updateAlbum(Integer id, AlbumDTO albumDetailsDTO) {
        if (albumRepository.findById(id).isEmpty()) {
            throw new ApiRuntimeException("Album not found with id " + id, 404);
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
        album.setUrl(albumDetailsDTO.getUrl());

        Album updatedAlbum = albumRepository.save(album);
        albumDetailsDTO.setId(updatedAlbum.getId());
        return albumDetailsDTO;
    }

    @Override
    public void deleteAlbum(Integer id) {
        if (!albumRepository.existsById(id)) {
            throw new ApiRuntimeException("Album not found with id " + id, 404);
        }
        
        Album album = albumRepository.findById(id).get();
        for (Song song : album.getSongs()) {
            song.setAlbum(null);
            songRepository.save(song);
        }

        Iterable<History> histories = historyRepository.findAll();
        for (History history : histories) {
            if (history.getType().equals("album") && history.getIdEntity().equals(id)) {
                historyRepository.delete(history);
            }
        }

        albumRepository.deleteById(id);
        historyVoid.deleteEntries("Album", id);
    }

    @Override
    public List<AlbumDTO> filterAlbums(FilterStruct struct) {
        Sort sort = Sort.unsorted();
        for (FilterStruct.SortCriteria sortCriteria : struct.getListOrderCriteria()) {
            Sort.Direction direction = sortCriteria.getValuesorOrder() == FilterStruct.SortValue.ASC ? 
                    Sort.Direction.ASC : Sort.Direction.DESC;
            sort = sort.and(Sort.by(direction, sortCriteria.getSortBy()));
        }

        Pageable pageable = PageRequest.of(struct.getPage().getPageIndex(), struct.getPage().getPageSize(), sort);
        Specification<Album> specification = AlbumSpecification.getAlbumsByFilters(struct.getListSearchCriteria());

        Page<Album> albums = albumRepository.findAll(specification, pageable);
        return albums.stream()
                .map(dtoUtil::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public AlbumDTO patchAlbum(Integer id, Album albumDetails) {
        Optional<Album> albumOptional = albumRepository.findById(id);
        if (!albumOptional.isPresent()) {
            throw new ApiRuntimeException("Album not found with id : " + id, 404);
        }

        Album album = albumOptional.get();
        if (albumDetails.getTitle() != null) {
            if (albumRepository.findByTitle(albumDetails.getTitle()) != null) {
                if (!album.getTitle().equals(albumDetails.getTitle())) {
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

        if (albumDetails.getSongs() != null) {
            album.getSongs().clear();
            Set<Song> existingSongs = new HashSet<>();
            for (Song song : albumDetails.getSongs()) {
                Song existingSong = songRepository.findByTitle(song.getTitle());
                if (existingSong == null) {
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
        return dtoUtil.convertToDto(updatedAlbum);
    }
}