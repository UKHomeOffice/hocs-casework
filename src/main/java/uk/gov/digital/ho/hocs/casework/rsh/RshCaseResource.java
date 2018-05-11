package uk.gov.digital.ho.hocs.casework.rsh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.dto.CaseSummaryResponse;
import uk.gov.digital.ho.hocs.casework.dto.SearchRequest;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
public class RshCaseResource {

    private final RshCaseService rshCaseService;
    private final AuditService auditService;

    @Autowired
    public RshCaseResource(RshCaseService rshCaseService, AuditService auditService) {
        this.rshCaseService = rshCaseService;
        this.auditService = auditService;
    }

    @RequestMapping(value = "/rsh/create", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<CaseSummaryResponse>> rshCreateCase(@RequestBody String data) {
        RshCaseDetails rshCaseDetails = rshCaseService.createRSHCase(data);
        this.auditService.createAuditEntry(rshCaseDetails.getUuid(), "CREATE", data);
        return Mono.justOrEmpty(ResponseEntity.ok(CaseSummaryResponse.from(rshCaseDetails)));
    }

    @RequestMapping(value = "/rsh/case/{uuid}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<CaseSummaryResponse>> rshUpdateCase(@PathVariable String uuid, @RequestBody String data) {
        RshCaseDetails rshCaseDetails = rshCaseService.updateRSHCase(uuid,data);
        return Mono.justOrEmpty(ResponseEntity.ok(CaseSummaryResponse.from(rshCaseDetails)));
    }

    @RequestMapping(value = "/rsh/case/{uuid}", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<RshCaseDetails>> rshGetCase(@PathVariable String uuid) {
        RshCaseDetails caseDetails = rshCaseService.getRSHCase(uuid);
        return Mono.justOrEmpty(ResponseEntity.ok(caseDetails));
    }

    @RequestMapping(value = "/rsh/search", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<List<RshCaseDetails>>> rshSearch(@RequestBody SearchRequest data) {
        List<RshCaseDetails> caseDetails = rshCaseService.findCases(data);
        return Mono.justOrEmpty(ResponseEntity.ok(caseDetails));
    }
}
