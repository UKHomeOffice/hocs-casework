package uk.gov.digital.ho.hocs.casework.api.overview;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ColumnSort {

    public enum SortOrder {
        ASCENDING,
        DESCENDING
    }

    @Getter
    private final String name;

    @Getter
    private final SortOrder order;
}
