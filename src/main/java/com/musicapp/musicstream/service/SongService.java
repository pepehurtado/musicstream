package com.musicapp.musicstream.service;

import com.musicapp.musicstream.dto.PagedResponse;
import com.musicapp.musicstream.dto.SongDTO;
import com.musicapp.musicstream.entities.FilterStruct;
import com.musicapp.musicstream.entities.Song;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface SongService {
    SongDTO createSong(Song song);
    List<SongDTO> getAllSongs(String title, Integer time, String url, String album);
    SongDTO getSongById(Integer id);
    SongDTO getSongByName(String name);
    SongDTO updateSong(Integer id, Song song);
    void deleteSong(Integer id);
    PagedResponse<SongDTO> filterSongs(FilterStruct filter);
    SongDTO patchSong(Integer id, Song song);
    SongDTO getRandomSong();
}