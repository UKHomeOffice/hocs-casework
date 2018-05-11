package uk.gov.digital.ho.hocs.casework.rsh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import uk.gov.digital.ho.hocs.casework.dto.CaseSaveRequest;
import uk.gov.digital.ho.hocs.casework.dto.CaseSaveResponse;
import uk.gov.digital.ho.hocs.casework.dto.SearchRequest;
import uk.gov.digital.ho.hocs.casework.dto.SearchResponse;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
public class RshCaseResource {

    private final RshCaseService rshCaseService;

    @Autowired
    public RshCaseResource(RshCaseService rshCaseService) {
        this.rshCaseService = rshCaseService;
    }

    @RequestMapping(value = "/rsh/create", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<CaseSaveResponse>> rshCreateCase(@RequestBody CaseSaveRequest request) {
        RshCaseDetails rshCaseDetails = rshCaseService.createRSHCase(request);
        return Mono.justOrEmpty(ResponseEntity.ok(CaseSaveResponse.from(rshCaseDetails)));
    }

    @RequestMapping(value = "/rsh/case/{uuid}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<CaseSaveResponse>> rshUpdateCase(@PathVariable String uuid, @RequestBody CaseSaveRequest request) {
        RshCaseDetails rshCaseDetails = rshCaseService.updateRSHCase(uuid,request);
        return Mono.justOrEmpty(ResponseEntity.ok(CaseSaveResponse.from(rshCaseDetails)));
    }

    @RequestMapping(value = "/rsh/case/{uuid}", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<RshCaseDetails>> rshGetCase(@PathVariable String uuid) {
        RshCaseDetails caseDetails = rshCaseService.getRSHCase(uuid);
        return Mono.justOrEmpty(ResponseEntity.ok(caseDetails));
    }

    @RequestMapping(value = "/rsh/search", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<List<SearchResponse>>> rshSearch(@RequestBody SearchRequest data) {
        List<SearchResponse> searchResponses = rshCaseService.findCases(data);
        return Mono.justOrEmpty(ResponseEntity.ok(searchResponses));
    }
}
