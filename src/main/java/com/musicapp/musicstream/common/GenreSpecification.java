package com.musicapp.musicstream.common;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.musicapp.musicstream.entities.FilterStruct;
import com.musicapp.musicstream.entities.Genre;
import com.musicapp.musicstream.entities.Song;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public class GenreSpecification {

    public static Specification<Genre> getGenresByFilters(List<FilterStruct.SearchCriteria> searchCriteriaList) {
        return (root, query, criteriaBuilder) -> {
            
            Predicate[] predicates = searchCriteriaList.stream()
                    .map(searchCriteria -> {
                        switch (searchCriteria.getOperation()) {
                            case EQUALS -> {
                            if ("songList".equals(searchCriteria.getKey())) {
                                Join<Genre, Song> songJoin = root.join("songList", JoinType.INNER);
                                return criteriaBuilder.equal(songJoin.get("id"), Long.valueOf(searchCriteria.getValue()));
                            } else {
                                return criteriaBuilder.equal(root.get(searchCriteria.getKey()), searchCriteria.getValue());
                            }
                    }
                            case CONTAINS -> {
                                return criteriaBuilder.like(root.get(searchCriteria.getKey()), "%" + searchCriteria.getValue() + "%");
                    }
                            case GREATER_THAN -> {
                                return criteriaBuilder.greaterThan(root.get(searchCriteria.getKey()), searchCriteria.getValue());
                    }
                            case LESS_THAN -> {
                                return criteriaBuilder.lessThan(root.get(searchCriteria.getKey()), searchCriteria.getValue());
                    }
                            default -> throw new UnsupportedOperationException("Operation not supported");
                        }
                    })
                    .toArray(Predicate[]::new);
            return criteriaBuilder.and(predicates);
        };
    }

    public static Specification<Genre> hasName(String name) {
        return (root, query, criteriaBuilder) -> 
        name == null ? null : criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }

    public static Specification<Genre> hasYear(Integer year) {
        return (root, query, criteriaBuilder) -> 
            year == null ? null : criteriaBuilder.equal(root.get("year"), year);
    }

    public static Specification<Genre> hasDescription(String description) {
        return (root, query, criteriaBuilder) -> 
            description == null ? null : criteriaBuilder.equal(root.get("description"), description);
    }

}
