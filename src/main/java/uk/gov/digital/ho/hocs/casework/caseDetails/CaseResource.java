package uk.gov.digital.ho.hocs.casework.caseDetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.audit.AuditAction;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.model.*;
import uk.gov.digital.ho.hocs.casework.notify.NotifyService;

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
    public ResponseEntity<CaseSaveResponse> createCase(@RequestBody CaseCreateRequest request, @RequestHeader("X-Auth-Username") String username) {
        CaseDetails caseDetails = caseService.createCase(request.getCaseType());
        auditService.createAuditEntry(caseDetails.getUuid(), AuditAction.CREATE, username, request.toJsonString(objectMapper));
        return ResponseEntity.ok(CaseSaveResponse.from(caseDetails));
    }

    @RequestMapping(value = "/case/{uuid}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity updateCase(@PathVariable UUID caseUUID, @RequestBody UpdateStageRequest request, @RequestHeader("X-Auth-Username") String username) {
        StageDetails stageDetails = caseService.updateStage(request.getStageUUID(), request.getSchemaVersion(), request.getStageData());
        auditService.createAuditEntry(stageDetails, AuditAction.UPDATE, username, request.toJsonString(objectMapper));
        return ResponseEntity.ok().build();
    }
}
