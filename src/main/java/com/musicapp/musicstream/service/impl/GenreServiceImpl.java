package com.musicapp.musicstream.service.impl;

import com.musicapp.musicstream.common.GenreSpecification;
import com.musicapp.musicstream.common.HistoryVoid;
import com.musicapp.musicstream.dto.DTOUtils;
import com.musicapp.musicstream.dto.GenreDTO;
import com.musicapp.musicstream.entities.*;
import com.musicapp.musicstream.exception.ApiRuntimeException;
import com.musicapp.musicstream.repository.GenreRepository;
import com.musicapp.musicstream.repository.SongRepository;
import com.musicapp.musicstream.service.GenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final SongRepository songRepository;
    private final DTOUtils dtoUtils;
    private final HistoryVoid historyVoid;

    @Autowired
    public GenreServiceImpl(GenreRepository genreRepository,
                          SongRepository songRepository,
                          DTOUtils dtoUtils,
                          HistoryVoid historyVoid) {
        this.genreRepository = genreRepository;
        this.songRepository = songRepository;
        this.dtoUtils = dtoUtils;
        this.historyVoid = historyVoid;
    }

    @Override
    public GenreDTO createGenre(Genre genre) {
        if (genreRepository.findByName(genre.getName()) != null) {
            throw new ApiRuntimeException("Genre already exists: " + genre.getName(), 409);
        }

        Genre newGenre = new Genre();
        newGenre.setName(genre.getName());
        newGenre.setDescription(genre.getDescription());
        newGenre.setYear(genre.getYear());

        if (genre.getSongList() != null) {
            processSongsForGenre(genre, newGenre);
        }

        Genre savedGenre = genreRepository.save(newGenre);
        historyVoid.createEntry("Genre", savedGenre.getId());
        
        return dtoUtils.convertToDto(savedGenre);
    }

    private void processSongsForGenre(Genre sourceGenre, Genre targetGenre) {
        for (Song song : sourceGenre.getSongList()) {
            Optional<Song> existingSong = songRepository.findById(song.getId());
            if (!existingSong.isPresent()) {
                throw new ApiRuntimeException("Song not found with id: " + song.getId(), 412);
            }
            targetGenre.addSong(existingSong.get());
        }
    }

    @Override
    public List<GenreDTO> getAllGenres(String name, Integer year, String description) {
        Specification<Genre> spec = Specification.where(GenreSpecification.hasName(name))
                .and(GenreSpecification.hasYear(year))
                .and(GenreSpecification.hasDescription(description));

        List<Genre> genres = genreRepository.findAll(spec);
        return genres.stream()
                .map(dtoUtils::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public GenreDTO getGenreById(Integer id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ApiRuntimeException("Genre not found with id: " + id, 404));
        return dtoUtils.convertToDto(genre);
    }

    @Override
    public GenreDTO updateGenre(Integer id, Genre genreDetails) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ApiRuntimeException("Genre not found with id: " + id, 404));

        genre.setName(genreDetails.getName());
        genre.setDescription(genreDetails.getDescription());
        genre.setYear(genreDetails.getYear());

        Genre updatedGenre = genreRepository.save(genre);
        return dtoUtils.convertToDto(updatedGenre);
    }

    @Override
    public void deleteGenre(Integer id) {
        if (!genreRepository.existsById(id)) {
            throw new ApiRuntimeException("Genre not found with id: " + id, 404);
        }

        genreRepository.deleteById(id);
        historyVoid.deleteEntries("Genre", id);
    }

    @Override
    public List<GenreDTO> filterGenres(FilterStruct filter) {
        Sort sort = buildSort(filter.getListOrderCriteria());
        Pageable pageable = PageRequest.of(filter.getPage().getPageIndex(), 
                                         filter.getPage().getPageSize(), 
                                         sort);

        Specification<Genre> specification = GenreSpecification.getGenresByFilters(filter.getListSearchCriteria());
        Page<Genre> genres = genreRepository.findAll(specification, pageable);

        return genres.stream()
                .map(dtoUtils::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public GenreDTO patchGenre(Integer id, Genre genreDetails) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ApiRuntimeException("Genre not found with id: " + id, 404));

        if (genreDetails.getName() != null) {
            genre.setName(genreDetails.getName());
        }
        if (genreDetails.getDescription() != null) {
            genre.setDescription(genreDetails.getDescription());
        }
        if (genreDetails.getYear() != null) {
            genre.setYear(genreDetails.getYear());
        }

        if (genreDetails.getSongList() != null) {
            updateGenreSongs(genre, genreDetails);
        }

        Genre updatedGenre = genreRepository.save(genre);
        return dtoUtils.convertToDto(updatedGenre);
    }

    private void updateGenreSongs(Genre genre, Genre genreDetails) {
        genre.getSongList().clear();
        for (Song song : genreDetails.getSongList()) {
            Song existingSong = songRepository.findById(song.getId())
                    .orElseThrow(() -> new ApiRuntimeException("Song not found with id: " + song.getId(), 412));
            genre.addSong(existingSong);
        }
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