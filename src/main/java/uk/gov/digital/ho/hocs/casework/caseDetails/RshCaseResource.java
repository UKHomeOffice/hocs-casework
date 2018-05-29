package uk.gov.digital.ho.hocs.casework.caseDetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
public class RshCaseResource {

    private final CaseService caseService;
    private final AuditService auditService;
    private final NotifyService notifyService;

    private CsvMapper csvMapper;

    private ObjectMapper objectMapper;

    @Autowired
    public RshCaseResource(CaseService caseService, AuditService auditService, NotifyService notifyService, ObjectMapper objectMapper) {
        this.caseService = caseService;
        this.auditService = auditService;
        this.notifyService = notifyService;
        this.objectMapper = objectMapper;
        this.csvMapper = new CsvMapper();
        HocsCaseServiceConfiguration.initialiseObjectMapper(this.csvMapper);
    }

    @RequestMapping(value = "/caseDetails/create", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<CaseSaveResponse>> rshCreateCase(@RequestBody RshCaseCreateRequest request, @RequestHeader("X-Auth-Username") String username) {
        CaseDetails caseDetails = caseService.createRshCase(request.getCaseData());

        auditService.createAuditEntry(caseDetails.getUuid(), AuditAction.CREATE, username, request.toJsonString(objectMapper));
        notifyService.sendRshNotify(request.getNotifyRequest(),caseDetails.getUuid());
        return Mono.justOrEmpty(ResponseEntity.ok(CaseSaveResponse.from(caseDetails)));
    }

    @RequestMapping(value = "/caseDetails/case/{caseUUID}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity rshUpdateCase(@PathVariable UUID caseUUID, @RequestBody RshCaseCreateRequest request, @RequestHeader("X-Auth-Username") String username) {
        CaseDetails caseDetails = caseService.updateRshCase(caseUUID,request.getCaseData());

        auditService.createAuditEntry(caseUUID, AuditAction.UPDATE, username, request.toJsonString(objectMapper));
        notifyService.sendRshNotify(request.getNotifyRequest(),caseUUID);
        return ResponseEntity.ok(CaseSaveResponse.from(caseDetails));
    }

    @RequestMapping(value = "/caseDetails/case/{caseUUID}", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CaseDetails> rshGetCase(@PathVariable UUID caseUUID, @RequestHeader("X-Auth-Username") String username) {
        CaseDetails caseDetails = caseService.getRSHCase(caseUUID);

        auditService.createAuditEntry(caseUUID, AuditAction.GET, username, caseUUID.toString());
        return ResponseEntity.ok(caseDetails);
    }

    @RequestMapping(value = "/caseDetails/search", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<CaseDetails>> rshSearch(@RequestBody SearchRequest data, @RequestHeader("X-Auth-Username") String username) {
        List<CaseDetails> searchResponses = caseService.findCases(data);

        auditService.createAuditEntry(AuditAction.SEARCH, username, data.toJsonString(objectMapper));
        return ResponseEntity.ok(searchResponses);
    }

    @RequestMapping(value = "/caseDetails/report/current", method = RequestMethod.GET, produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> rshReportCurrent(@RequestHeader("X-Auth-Username") String username) {
        return ResponseEntity.ok("HELLO!");
    }

    @RequestMapping(value = "/caseDetails/report/cutoff/{cutoff}", method = RequestMethod.GET, produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> rshReportCutoff(@PathVariable("cutoff") String cutoff, @RequestHeader("X-Auth-Username") String username) {
        return ResponseEntity.ok("HELLO!, Cutoff!");
    }
}
