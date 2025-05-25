package com.musicapp.musicstream.service;

import com.musicapp.musicstream.dto.ArtistDTO;
import com.musicapp.musicstream.entities.Artist;
import com.musicapp.musicstream.entities.FilterStruct;

import org.springframework.data.domain.Page;

import java.util.List;

public interface ArtistService {
    ArtistDTO createArtist(Artist artist);
    List<ArtistDTO> getAllArtists(String name, String country, Integer age, String dateOfBirth);
    ArtistDTO getArtistById(Integer id);
    ArtistDTO getArtistByName(String name);
    ArtistDTO updateArtist(Integer id, ArtistDTO artistDTO);
    void deleteArtist(Integer id);
    Page<ArtistDTO> filterArtists(FilterStruct struct);
    ArtistDTO patchArtist(Integer id, Artist artist);
}