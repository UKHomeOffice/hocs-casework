package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.api.dto.GetStagesResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.SearchRequest;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import javax.validation.Valid;
import java.util.Set;

@Slf4j
@RestController
class SearchResource {

    private final SearchService searchService;

    @Autowired
    public SearchResource(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping(value = "/search")
    ResponseEntity<GetStagesResponse> search(@Valid @RequestBody SearchRequest request) {
        Set<StageWithCaseData> stages = searchService.search(request);
        return ResponseEntity.ok(GetStagesResponse.from(stages));
    }

}
