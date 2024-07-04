package com.musicapp.musicstream.common;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.musicapp.musicstream.entities.Album;
import com.musicapp.musicstream.entities.FilterStruct;
import com.musicapp.musicstream.entities.Song;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public class SongSpecification {

    public static Specification<Song> getSongsByFilters(List<FilterStruct.SearchCriteria> searchCriteriaList) {
        return (root, query, criteriaBuilder) -> {
            
            Predicate[] predicates = searchCriteriaList.stream()
                    .map(searchCriteria -> {
                        switch (searchCriteria.getOperation()) {
                            case EQUALS -> {
                            if ("album".equals(searchCriteria.getKey())) {
                                Join<Song, Album> albumJoin = root.join("album", JoinType.INNER);
                                return criteriaBuilder.equal(albumJoin.get("id"), Long.valueOf(searchCriteria.getValue()));
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

    public static Specification<Song> hasTitle(String title) {
        return (root, query, criteriaBuilder) -> 
        title == null ? null : criteriaBuilder.like(root.get("title"), "%" + title + "%");
    }

    public static Specification<Song> hasTime(Integer time) {
        return (root, query, criteriaBuilder) -> 
            time == null ? null : criteriaBuilder.equal(root.get("time"), time);
    }

    public static Specification<Song> hasUrl(String url) {
        return (root, query, criteriaBuilder) -> 
            url == null ? null : criteriaBuilder.equal(root.get("url"), url);
    }

    public static Specification<Song> hasAlbum(Integer albumId) {
        return (root, query, criteriaBuilder) -> 
            albumId == null ? null : criteriaBuilder.equal(root.get("album").get("id"), albumId);
    }

}
