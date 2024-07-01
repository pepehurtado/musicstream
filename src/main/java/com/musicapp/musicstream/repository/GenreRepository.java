package com.musicapp.musicstream.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import com.musicapp.musicstream.entities.Genre;

public interface GenreRepository extends CrudRepository<Genre, Integer>, JpaSpecificationExecutor<Genre> {
    Genre findByName(String name);
}
