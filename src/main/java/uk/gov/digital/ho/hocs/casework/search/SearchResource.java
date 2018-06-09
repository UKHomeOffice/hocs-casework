package uk.gov.digital.ho.hocs.casework.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseDetails;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
class SearchResource {

    private final SearchService searchService;

    @Autowired
    public SearchResource(SearchService searchService) {

        this.searchService = searchService;
    }

    @RequestMapping(value = "/rsh/search", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<CaseDetails>> search(@RequestBody SearchRequest data, @RequestHeader("X-Auth-Username") String username) {
        List<CaseDetails> searchResponses = searchService.findCases(data, username);
        return ResponseEntity.ok(searchResponses);
    }
}
