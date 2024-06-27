package com.musicapp.musicstream.repository;

import org.springframework.data.repository.CrudRepository;

import com.musicapp.musicstream.entities.Album;

public interface AlbumRepository extends CrudRepository<Album, Integer> {
    Album findByTitle(String name);
}
