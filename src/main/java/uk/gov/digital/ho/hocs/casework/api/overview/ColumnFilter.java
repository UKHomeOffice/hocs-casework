package uk.gov.digital.ho.hocs.casework.api.overview;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ColumnFilter {

    public enum FilterType {
        BOOLEAN_VALUE,
        EQUALS,
        IN,
        CASE_INSENSITIVE_LIKE,
        JOIN_BY_CODE
    }

    @Getter
    private final String name;

    @Getter
    private final String value;

    @Getter
    private final FilterType filterType;
}
