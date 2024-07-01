package com.musicapp.musicstream.dto;

import java.util.Set;

import lombok.Data;

@Data
public class GenreDTO {
    private Integer id;
    private String name;
    private String description;
    private Integer year;
    private Set<Integer> songList;
}
