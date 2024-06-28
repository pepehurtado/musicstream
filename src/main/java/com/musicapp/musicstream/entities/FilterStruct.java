package com.musicapp.musicstream.entities;

import java.util.List;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class FilterStruct {

    private List<SortCriteria> listOrderCriteria;
    private List<SearchCriteria> listSearchCriteria;
    private Page page;

    @Data
    public static class SortCriteria {
        private String sortBy;
        @Enumerated(value = EnumType.STRING)
    private SortValue valuesorOrder;
    }
    public enum SortValue {
        ASC,
        DESC
    }


    @Data
    public static class SearchCriteria {
        private String key;
        private CriteriaOperation operation;
        private String value;
    }

    public enum CriteriaOperation {
        EQUALS,
        CONTAINS,
        GREATER_THAN,
        LESS_THAN
    }

    @Data
    public static class Page {
        private int pageIndex;
        private int pageSize;

    }
}
