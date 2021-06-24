package uk.gov.digital.ho.hocs.casework.api.overview;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;

/**
 * A query for requesting paged, sorted and filtered data from a service.
 */
@Data
@AllArgsConstructor
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int pageNumber;

    private final int resultsPerPage;

    protected List<ColumnSort> columnSorts;

    protected List<ColumnFilter>  columnFilters;

    public int getOffset() {
        return pageNumber * resultsPerPage;
    }

}
