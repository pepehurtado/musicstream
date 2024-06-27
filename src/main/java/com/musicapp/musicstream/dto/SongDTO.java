package com.musicapp.musicstream.dto;

import java.util.Date;
import java.util.Set;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SongDTO {
    private Integer id;
    private String title;
    private Date time;
    private String url;
    private Set<ArtistDTO> artists;
    private AlbumDTO album;

    // Getters and Setters
}
