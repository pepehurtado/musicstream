package com.musicapp.musicstream.common;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.musicapp.musicstream.entities.Album;
import com.musicapp.musicstream.entities.Artist;
import com.musicapp.musicstream.entities.FilterStruct;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public class AlbumSpecification {

    public static Specification<Album> getAlbumsByFilters(List<FilterStruct.SearchCriteria> searchCriteriaList) {
        return (root, query, criteriaBuilder) -> {
            Predicate[] predicates = searchCriteriaList.stream()
                    .map(searchCriteria -> {
                        switch (searchCriteria.getOperation()) {
                            case EQUALS -> {
                                if("artist".equals(searchCriteria.getKey())) {
                                    Join<Album, Artist> artistJoin = root.join("artist", JoinType.INNER);
                                    return criteriaBuilder.equal(artistJoin.get("id"), Long.valueOf(searchCriteria.getValue()));
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

    public static Specification<Album> hasTitle(String title) {
        return (root, query, criteriaBuilder) -> 
        title == null ? null : criteriaBuilder.like(root.get("title"), "%" + title + "%");
    }

    public static Specification<Album> hasYear(Integer year) {
        return (root, query, criteriaBuilder) -> 
            year == null ? null : criteriaBuilder.equal(root.get("year"), year);
    }

    public static Specification<Album> hasUrl(String url) {
        return (root, query, criteriaBuilder) -> 
            url == null ? null : criteriaBuilder.equal(root.get("url"), url);
    }

    public static Specification<Album> hasNumberOfSongs(Integer numberOfSongs) {
        return (root, query, criteriaBuilder) -> 
            numberOfSongs == null ? null : criteriaBuilder.equal(root.get("numberOfSongs"), numberOfSongs);
    }

}
