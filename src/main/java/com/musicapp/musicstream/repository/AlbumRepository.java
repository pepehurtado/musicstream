package com.musicapp.musicstream.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import com.musicapp.musicstream.entities.Album;

public interface AlbumRepository extends CrudRepository<Album, Integer>, JpaSpecificationExecutor<Album> {
    Album findByTitle(String name);
}
