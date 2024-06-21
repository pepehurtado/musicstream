package com.musicapp.musicstream.repository;

import org.springframework.data.repository.CrudRepository;

import com.musicapp.musicstream.entities.Artist;

public interface ArtistRepository extends CrudRepository<Artist, Integer> {
    Artist findByName(String name);
}
