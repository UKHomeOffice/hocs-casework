package uk.gov.digital.ho.hocs.casework.api.overview;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.digital.ho.hocs.casework.domain.model.CaseOverview;

@RestController
class OverviewResource {

    protected final OverviewService overviewService;
    protected final PageRequestFactory pageRequestFactory;

    @Autowired
    public OverviewResource(OverviewService overviewService, PageRequestFactory pageRequestFactory) {
        this.overviewService = overviewService;
        this.pageRequestFactory = pageRequestFactory;
    }

    @GetMapping(value = "/overview")
    ResponseEntity<Page<CaseOverview>> getOverview(
        @RequestParam(name = "pageSize") int pageSize,
        @RequestParam(name = "pageIndex") int pageIndex,
        @RequestParam(value = "filter", required = false) String filterCriteriaString,
        @RequestParam(value = "sort", required = false) String sortCriteriaString) {
        PageRequest pageRequest = pageRequestFactory.build(pageIndex, pageSize, filterCriteriaString, sortCriteriaString);
        Page<CaseOverview> overviewData = overviewService.getOverview(pageRequest);
        return ResponseEntity.ok(overviewData);
    }

}
