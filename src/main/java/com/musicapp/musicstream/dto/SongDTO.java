package com.musicapp.musicstream.dto;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SongDTO {
    private Integer id;
    private String title;
    private Integer time;
    private String url;
    private Set<ArtistDTO> artists;
    private AlbumDTO album;
    private Set<GenreDTO> genreList = new HashSet<>();

}
