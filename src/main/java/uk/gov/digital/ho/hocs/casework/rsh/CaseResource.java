package uk.gov.digital.ho.hocs.casework.rsh;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.audit.AuditAction;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.model.*;
import uk.gov.digital.ho.hocs.casework.notify.NotifyService;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
public class CaseResource {

    private final CaseService caseService;
    private final AuditService auditService;
    private final NotifyService notifyService;

    private CsvMapper csvMapper;

    private ObjectMapper objectMapper;

    @Autowired
    public CaseResource(CaseService caseService, AuditService auditService, NotifyService notifyService, ObjectMapper objectMapper) {
        this.caseService = caseService;
        this.auditService = auditService;
        this.notifyService = notifyService;
        this.objectMapper = objectMapper;
        this.csvMapper = new CsvMapper();
        HocsCaseServiceConfiguration.initialiseObjectMapper(this.csvMapper);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<CaseSaveResponse>> createCase(@RequestBody CaseCreateRequest request, @RequestHeader("X-Auth-Username") String username) {
        CorrelationDetails correlationDetails = new CorrelationDetails(username);
        CaseDetails caseDetails = caseService.createCase(request.getCaseType());
        auditService.createAuditEntry(caseDetails, AuditAction.CREATE, correlationDetails, request.toJsonString(objectMapper));
        return Mono.justOrEmpty(ResponseEntity.ok(CaseSaveResponse.from(caseDetails)));
    }

    @RequestMapping(value = "/case/{uuid}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity> updateCase(@PathVariable UUID caseUUID, @RequestBody UpdateStageRequest request, @RequestHeader("X-Auth-Username") String username) {
        CorrelationDetails correlationDetails = new CorrelationDetails(username);
        StageDetails stageDetails = caseService.updateStage(request.getStageUUID(), request.getSchemaVersion(), request.getStageData());
        auditService.createAuditEntry(stageDetails, AuditAction.UPDATE, correlationDetails, request.toJsonString(objectMapper));
        return Mono.justOrEmpty(ResponseEntity.ok().build());
    }
}
