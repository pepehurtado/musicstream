package com.musicapp.musicstream.common;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.musicapp.musicstream.entities.Artist;
import com.musicapp.musicstream.entities.FilterStruct;

import jakarta.persistence.criteria.Predicate;

public class ArtistSpecification {

    public static Specification<Artist> getArtistsByFilters(List<FilterStruct.SearchCriteria> searchCriteriaList) {
        return (root, query, criteriaBuilder) -> {
            Predicate[] predicates = searchCriteriaList.stream()
                    .map(searchCriteria -> {
                        switch (searchCriteria.getOperation()) {
                            case EQUALS -> {
                                return criteriaBuilder.equal(root.get(searchCriteria.getKey()), searchCriteria.getValue());
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
}
