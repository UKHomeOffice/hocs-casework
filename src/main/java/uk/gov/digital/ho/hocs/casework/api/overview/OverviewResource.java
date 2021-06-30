package uk.gov.digital.ho.hocs.casework.api.overview;

import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseOverview;

@RestController
@AllArgsConstructor
class OverviewResource {

    protected final OverviewService overviewService;
    protected final PageRequestFactory pageRequestFactory;
    protected final InfoClient infoClient;

    @GetMapping(value = "/overview")
    ResponseEntity<Page<CaseOverview>> getOverview(
        @RequestParam(name = "pageSize") int pageSize,
        @RequestParam(name = "pageIndex") int pageIndex,
        @RequestParam(value = "filter", required = false) String filterCriteriaString,
        @RequestParam(value = "sort", required = false) String sortCriteriaString) {
        Set<CaseTypeDto> permittedCaseTypes = infoClient.getCaseTypesForUser();
        PageRequest pageRequest = pageRequestFactory.build(pageIndex, pageSize, filterCriteriaString, sortCriteriaString, permittedCaseTypes);
        Page<CaseOverview> overviewData = overviewService.getOverview(pageRequest);
        return ResponseEntity.ok(overviewData);
    }

}
