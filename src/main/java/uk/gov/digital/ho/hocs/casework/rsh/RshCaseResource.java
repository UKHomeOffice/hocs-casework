package uk.gov.digital.ho.hocs.casework.rsh;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.gov.digital.ho.hocs.casework.audit.AuditAction;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.model.*;
import uk.gov.digital.ho.hocs.casework.notify.NotifyService;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
public class RshCaseResource {

    private final CaseService caseService;
    private final AuditService auditService;
    private final NotifyService notifyService;

    @Autowired
    public RshCaseResource(CaseService caseService, AuditService auditService, NotifyService notifyService) {
        this.caseService = caseService;
        this.auditService = auditService;
        this.notifyService = notifyService;
    }

    @RequestMapping(value = "/rsh/create", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<CaseSaveResponse>> rshCreateCase(@RequestBody RshCaseCreateRequest request, @RequestHeader("X-Auth-Username") String username) {
        CaseDetails caseDetails = caseService.createCase("XLS");
        caseService.createStage(caseDetails.getUuid(),"OnlyStage", 0, request.getCaseData());
        //auditService.createAuditEntry(caseDetails.getUuid(), AuditAction.CREATE, username, null);
        //notifyService.determineNotificationRequired(request.getNotifyDetails(),caseDetails.getUuid());
        return Mono.justOrEmpty(ResponseEntity.ok(CaseSaveResponse.from(caseDetails)));
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<CaseSaveResponse>> createCase(@RequestBody CaseCreateRequest request, @RequestHeader("X-Auth-Username") String username) {
        CaseDetails caseDetails = caseService.createCase(request.getCaseType());
        //auditService.createAuditEntry(caseDetails.getUuid(), AuditAction.CREATE, username, null);
        //notifyService.determineNotificationRequired(request.getNotifyDetails(),caseDetails.getUuid());
        return Mono.justOrEmpty(ResponseEntity.ok(CaseSaveResponse.from(caseDetails)));
    }

    @RequestMapping(value = "/rsh/case/{uuid}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity> rshUpdateCase(@PathVariable UUID caseUUID, @RequestBody RshCaseCreateRequest request, @RequestHeader("X-Auth-Username") String username) {
        caseService.updateStage(caseUUID, 0,request.getCaseData());
        //auditService.createAuditEntry(rshCaseDetails.getUuid(), AuditAction.UPDATE, username, request.getCaseData());
        //notifyService.determineNotificationRequired(request.getNotifyDetails(),rshCaseDetails.getUuid());
        return Mono.justOrEmpty(ResponseEntity.ok().build());
    }

    @RequestMapping(value = "/case/{uuid}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity> updateCase(@RequestBody UpdateStageRequest request, @RequestHeader("X-Auth-Username") String username) {
        caseService.updateStage(request.getStageUUID(), request.getSchemaVersion(), request.getStageData());
        //auditService.createAuditEntry(caseDetails.getUuid(), AuditAction.CREATE, username, null);
        //notifyService.determineNotificationRequired(request.getNotifyDetails(),caseDetails.getUuid());
        return Mono.justOrEmpty(ResponseEntity.ok().build());
    }

    @RequestMapping(value = "/rsh/case/{uuid}", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<CaseDetails>> rshGetCase(@PathVariable UUID uuid, @RequestHeader("X-Auth-Username") String username) {
        CaseDetails caseDetails = caseService.getRSHCase(uuid);
        //auditService.createAuditEntry(uuid, AuditAction.GET, username, null);
        //notifyService.determineNotificationRequired(request.getNotifyDetails(),caseDetails.getUuid());
        return Mono.justOrEmpty(ResponseEntity.ok(caseDetails));
    }

    @RequestMapping(value = "/rsh/search", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public Flux<ResponseEntity<List<CaseDetails>>> rshSearch(@RequestBody SearchRequest data, @RequestHeader("X-Auth-Username") String username) {
        List<CaseDetails> searchResponses = caseService.findCases(data);
        //auditService.createAuditEntry(data.getCaseReference(), AuditAction.SEARCH, username, data.getCaseData());
        return Flux.just(ResponseEntity.ok(searchResponses));
    }
}
