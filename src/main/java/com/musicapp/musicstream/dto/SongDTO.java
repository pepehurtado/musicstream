package com.musicapp.musicstream.dto;

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
    private Set<Integer> artists;
    private Integer album;
    private Set<Integer> genreList;

}
