package uk.gov.digital.ho.hocs.casework.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.search.dto.SearchRequest;

import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
class SearchResource {

    private final SearchService searchService;

    @Autowired
    public SearchResource(SearchService searchService) {

        this.searchService = searchService;
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
    ResponseEntity<Set<CaseData>> search(@RequestBody SearchRequest searchRequest) {
        Set<CaseData> searchResponses = searchService.findCases(searchRequest);
        return ResponseEntity.ok(searchResponses);
    }
}
