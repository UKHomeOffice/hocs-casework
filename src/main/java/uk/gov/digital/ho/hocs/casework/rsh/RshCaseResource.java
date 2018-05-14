package uk.gov.digital.ho.hocs.casework.rsh;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.dto.CaseSaveRequest;
import uk.gov.digital.ho.hocs.casework.dto.CaseSaveResponse;
import uk.gov.digital.ho.hocs.casework.dto.SearchRequest;
import uk.gov.digital.ho.hocs.casework.notify.NotifyService;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
public class RshCaseResource {

    private final RshCaseService rshCaseService;
    private final AuditService auditService;
    private final NotifyService notifyService;

    @Autowired
    public RshCaseResource(RshCaseService rshCaseService, AuditService auditService, NotifyService notifyService) {
        this.rshCaseService = rshCaseService;
        this.auditService = auditService;
        this.notifyService = notifyService;
    }

    @RequestMapping(value = "/rsh/create", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<CaseSaveResponse>> rshCreateCase(@RequestBody CaseSaveRequest request, @RequestHeader("X-Auth-Username") String username) {
        RshCaseDetails rshCaseDetails = null;
        try {
            rshCaseDetails = rshCaseService.createRSHCase(request.getCaseData());
            auditService.createAuditEntry(rshCaseDetails.getUuid(), "CREATE", username, request.getCaseData());
            notifyService.determineNotificationRequired(request.getNotifyEmail(),request.getNotifyTeamName(),rshCaseDetails.getUuid());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Mono.justOrEmpty(ResponseEntity.badRequest().build());
        }
        return Mono.justOrEmpty(ResponseEntity.ok(CaseSaveResponse.from(rshCaseDetails)));
    }

    @RequestMapping(value = "/rsh/case/{uuid}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<CaseSaveResponse>> rshUpdateCase(@PathVariable String uuid, @RequestBody CaseSaveRequest request, @RequestHeader("X-Auth-Username") String username) {
        RshCaseDetails rshCaseDetails = null;
        try {
            rshCaseDetails = rshCaseService.updateRSHCase(uuid,request.getCaseData());
            auditService.createAuditEntry(rshCaseDetails.getUuid(), "UPDATE", username, request.getCaseData());
            notifyService.determineNotificationRequired(request.getNotifyEmail(),request.getNotifyTeamName(),rshCaseDetails.getUuid());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Mono.justOrEmpty(ResponseEntity.badRequest().build());
        }
        return Mono.justOrEmpty(ResponseEntity.ok(CaseSaveResponse.from(rshCaseDetails)));
    }

    @RequestMapping(value = "/rsh/case/{uuid}", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<RshCaseDetails>> rshGetCase(@PathVariable String uuid, @RequestHeader("X-Auth-Username") String username) {
        RshCaseDetails caseDetails = rshCaseService.getRSHCase(uuid);
        try {
            auditService.createAuditEntry(uuid, "RETRIEVE", username, null);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Mono.justOrEmpty(ResponseEntity.badRequest().build());
        }
        return Mono.justOrEmpty(ResponseEntity.ok(caseDetails));
    }

    @RequestMapping(value = "/rsh/search", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<List<RshCaseDetails>>> rshSearch(@RequestBody SearchRequest data, @RequestHeader("X-Auth-Username") String username) {
        List<RshCaseDetails> searchResponses = rshCaseService.findCases(data);
        try {
            auditService.createAuditEntry(data.getCaseReference(), "SEARCH", username, data.getCaseData());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Mono.justOrEmpty(ResponseEntity.badRequest().build());
        }
        return Mono.justOrEmpty(ResponseEntity.ok(searchResponses));
    }
}
