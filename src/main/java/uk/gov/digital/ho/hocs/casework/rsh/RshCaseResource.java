package uk.gov.digital.ho.hocs.casework.rsh;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import uk.gov.digital.ho.hocs.casework.audit.AuditAction;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.model.CaseSaveRequest;
import uk.gov.digital.ho.hocs.casework.model.CaseSaveResponse;
import uk.gov.digital.ho.hocs.casework.model.RshCaseSaveRequest;
import uk.gov.digital.ho.hocs.casework.model.SearchRequest;
import uk.gov.digital.ho.hocs.casework.notify.NotifyService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    public Mono<ResponseEntity<CaseSaveResponse>> rshCreateCase(@RequestBody RshCaseSaveRequest request, @RequestHeader("X-Auth-Username") String username) {
        try {
            caseDetails = rshCaseService.createRSHCase(request);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Mono.justOrEmpty(ResponseEntity.badRequest().build());
        }

        return Mono.justOrEmpty(ResponseEntity.ok(CaseSaveResponse.from(caseDetails)));
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<CaseSaveResponse>> createCase(@RequestBody CaseSaveRequest request, @RequestHeader("X-Auth-Username") String username) {
        CaseDetails caseDetails;
        try {
            caseDetails = rshCaseService.createCase(request);
            auditService.createAuditEntry(caseDetails.getUuid(), AuditAction.CREATE, username, request.getCaseData());
            notifyService.determineNotificationRequired(request.getNotifyDetails(),caseDetails.getUuid());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Mono.justOrEmpty(ResponseEntity.badRequest().build());
        }
        return Mono.justOrEmpty(ResponseEntity.ok(CaseSaveResponse.from(caseDetails)));
    }

    @RequestMapping(value = "/rsh/case/{uuid}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<CaseSaveResponse>> rshUpdateCase(@PathVariable String uuid, @RequestBody CaseSaveRequest request, @RequestHeader("X-Auth-Username") String username) {
        CaseDetails rshCaseDetails;
        try {
            rshCaseDetails = rshCaseService.updateRSHCase(uuid,request.getCaseData());
            auditService.createAuditEntry(rshCaseDetails.getUuid(), AuditAction.UPDATE, username, request.getCaseData());
            notifyService.determineNotificationRequired(request.getNotifyDetails(),rshCaseDetails.getUuid());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Mono.justOrEmpty(ResponseEntity.badRequest().build());
        }
        return Mono.justOrEmpty(ResponseEntity.ok(CaseSaveResponse.from(rshCaseDetails)));
    }

    @RequestMapping(value = "/rsh/case/{uuid}", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<CaseDetails>> rshGetCase(@PathVariable String uuid, @RequestHeader("X-Auth-Username") String username) {
        CaseDetails caseDetails = rshCaseService.getRSHCase(uuid);
        try {
            auditService.createAuditEntry(uuid, AuditAction.GET, username, null);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Mono.justOrEmpty(ResponseEntity.badRequest().build());
        }
        return Mono.justOrEmpty(ResponseEntity.ok(caseDetails));
    }

    @RequestMapping(value = "/rsh/search", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<List<CaseDetails>>> rshSearch(@RequestBody SearchRequest data, @RequestHeader("X-Auth-Username") String username) {
        List<CaseDetails> searchResponses = rshCaseService.findCases(data);
        try {
            auditService.createAuditEntry(data.getCaseReference(), AuditAction.SEARCH, username, data.getCaseData());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Mono.justOrEmpty(ResponseEntity.badRequest().build());
        }
        return Mono.justOrEmpty(ResponseEntity.ok(searchResponses));
    }
}
