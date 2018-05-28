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
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@RestController
public class CaseRshResource {

    private final CaseService caseService;
    private final AuditService auditService;
    private final NotifyService notifyService;

    private CsvMapper csvMapper;

    private ObjectMapper objectMapper;

    @Autowired
    public CaseRshResource(CaseService caseService, AuditService auditService, NotifyService notifyService, ObjectMapper objectMapper) {
        this.caseService = caseService;
        this.auditService = auditService;
        this.notifyService = notifyService;
        this.objectMapper = objectMapper;
        this.csvMapper = new CsvMapper();
        HocsCaseServiceConfiguration.initialiseObjectMapper(this.csvMapper);
    }

    @RequestMapping(value = "/rsh/create", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<CaseSaveResponse>> rshCreateCase(@RequestBody RshCaseCreateRequest request, @RequestHeader("X-Auth-Username") String username) {
        CorrelationDetails correlationDetails = new CorrelationDetails(username);
        CaseDetails caseDetails = caseService.createRshCase(request.getCaseData());
        auditService.createAuditEntry(caseDetails, AuditAction.CREATE, correlationDetails, request.toJsonString(objectMapper));
        NotifyDetails notifyDetails = new NotifyDetails(request.getNotifyEmail(), request.getNotifyTeamName());
        notifyService.sendRshNotify(notifyDetails,caseDetails.getUuid());
        return Mono.justOrEmpty(ResponseEntity.ok(CaseSaveResponse.from(caseDetails)));
    }

    @RequestMapping(value = "/rsh/case/{caseUUID}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity> rshUpdateCase(@PathVariable UUID caseUUID, @RequestBody RshCaseCreateRequest request, @RequestHeader("X-Auth-Username") String username) {
        CorrelationDetails correlationDetails = new CorrelationDetails(username);
        CaseDetails caseDetails = caseService.updateRshCase(caseUUID,request.getCaseData());
        auditService.createAuditEntry(caseDetails, AuditAction.UPDATE, correlationDetails, request.toJsonString(objectMapper));
        NotifyDetails notifyDetails = new NotifyDetails(request.getNotifyEmail(), request.getNotifyTeamName());
        notifyService.sendRshNotify(notifyDetails,caseUUID);
        return Mono.justOrEmpty(ResponseEntity.ok(CaseSaveResponse.from(caseDetails)));
    }

    @RequestMapping(value = "/rsh/case/{caseUUID}", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public Mono<ResponseEntity<CaseDetails>> rshGetCase(@PathVariable UUID caseUUID, @RequestHeader("X-Auth-Username") String username) {
        CaseDetails caseDetails = caseService.getRSHCase(caseUUID);
        //auditService.createAuditEntry(uuid, AuditAction.GET, username, null);
        return Mono.justOrEmpty(ResponseEntity.ok(caseDetails));
    }

    @RequestMapping(value = "/rsh/search", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<CaseDetails>> rshSearch(@RequestBody SearchRequest data, @RequestHeader("X-Auth-Username") String username) {
        List<CaseDetails> searchResponses = caseService.findCases(data);
        //auditService.createAuditEntry(data.getCaseReference(), AuditAction.SEARCH, username, data.getCaseData());
        return ResponseEntity.ok(searchResponses);
    }

    @RequestMapping(value = "/rsh/report/current", method = RequestMethod.GET, produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> rshReportCurrent(@RequestHeader("X-Auth-Username") String username) {
        return ResponseEntity.ok("HELLO!");
    }

    @RequestMapping(value = "/rsh/report/cutoff/{cutoff}", method = RequestMethod.GET, produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> rshReportCutoff(@PathVariable("cutoff") String cutoff, @RequestHeader("X-Auth-Username") String username) {
        return ResponseEntity.ok("HELLO!, Cutoff!");
    }
}
