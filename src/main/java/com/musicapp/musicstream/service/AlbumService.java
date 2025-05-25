package com.musicapp.musicstream.service;

import com.musicapp.musicstream.dto.AlbumDTO;
import com.musicapp.musicstream.entities.Album;
import com.musicapp.musicstream.entities.FilterStruct;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AlbumService {
    void createAlbum(Album album);
    List<AlbumDTO> getAllAlbums(String title, Integer year, Integer numberOfSongs, String url);
    AlbumDTO getAlbumById(Integer id);
    AlbumDTO getAlbumByName(String name);
    AlbumDTO updateAlbum(Integer id, AlbumDTO albumDetailsDTO);
    void deleteAlbum(Integer id);
    List<AlbumDTO> filterAlbums(FilterStruct struct);
    AlbumDTO patchAlbum(Integer id, Album albumDetails);
}