package com.musicapp.musicstream.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AlbumDTO {
    private Integer id;
    private String title;
    private String year;
    private Integer artist;
    private String description;
    private Integer numberOfSongs;
    private String url;

    @JsonIgnore
    private Set<Integer> songs;
}
