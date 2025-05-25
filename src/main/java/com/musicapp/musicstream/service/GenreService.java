package com.musicapp.musicstream.service;

import com.musicapp.musicstream.dto.GenreDTO;
import com.musicapp.musicstream.entities.Genre;
import com.musicapp.musicstream.entities.FilterStruct;

import java.util.List;

public interface GenreService {
    GenreDTO createGenre(Genre genre);
    List<GenreDTO> getAllGenres(String name, Integer year, String description);
    GenreDTO getGenreById(Integer id);
    GenreDTO updateGenre(Integer id, Genre genre);
    void deleteGenre(Integer id);
    List<GenreDTO> filterGenres(FilterStruct filter);
    GenreDTO patchGenre(Integer id, Genre genre);
}