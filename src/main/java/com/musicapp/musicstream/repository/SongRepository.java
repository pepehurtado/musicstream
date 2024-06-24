package com.musicapp.musicstream.repository;

import org.springframework.data.repository.CrudRepository;

import com.musicapp.musicstream.entities.Song;

public interface SongRepository extends CrudRepository<Song, Integer> {
    Song findByTitle(String name);
}
