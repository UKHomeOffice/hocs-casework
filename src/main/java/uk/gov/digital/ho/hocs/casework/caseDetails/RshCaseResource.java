package uk.gov.digital.ho.hocs.casework.caseDetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.model.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
public class RshCaseResource {

    private final CaseService caseService;

    private CsvMapper csvMapper;

    private ObjectMapper objectMapper;

    @Autowired
    public RshCaseResource(CaseService caseService, ObjectMapper objectMapper) {

        this.caseService = caseService;
        this.objectMapper = objectMapper;
        this.csvMapper = new CsvMapper();
        HocsCaseServiceConfiguration.initialiseObjectMapper(this.csvMapper);
    }

    @RequestMapping(value = "/rsh/create", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CaseSaveResponse> rshCreateCase(@RequestBody RshCaseCreateRequest request, @RequestHeader("X-Auth-Username") String username) {
        CaseDetails caseDetails = caseService.createRshCase(request.getCaseData(), request.getNotifyRequest(), username);

        return ResponseEntity.ok(CaseSaveResponse.from(caseDetails));
    }

    @RequestMapping(value = "/rsh/case/{caseUUID}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CaseSaveResponse> rshUpdateCase(@PathVariable UUID caseUUID, @RequestBody RshCaseCreateRequest request, @RequestHeader("X-Auth-Username") String username) {
        CaseDetails caseDetails = caseService.updateRshCase(caseUUID, request.getCaseData(), request.getNotifyRequest(), username);
        return ResponseEntity.ok(CaseSaveResponse.from(caseDetails));
    }

    @RequestMapping(value = "/rsh/case/{caseUUID}", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CaseDetails> rshGetCase(@PathVariable UUID caseUUID, @RequestHeader("X-Auth-Username") String username) {
        CaseDetails caseDetails = caseService.getRSHCase(caseUUID,username);
        return ResponseEntity.ok(caseDetails);
    }

    @RequestMapping(value = "/rsh/search", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<CaseDetails>> rshSearch(@RequestBody SearchRequest data, @RequestHeader("X-Auth-Username") String username) {
        List<CaseDetails> searchResponses = caseService.findCases(data, username);
        return ResponseEntity.ok(searchResponses);
    }

    @RequestMapping(value = "/rsh/report/current", method = RequestMethod.GET, produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> rshReportCurrent(@RequestHeader("X-Auth-Username") String username) {

        return ResponseEntity.ok("HELLO!");
    }

    @RequestMapping(value = "/rsh/report/cutoff/{cutoff}", method = RequestMethod.GET, produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> rshReportCutoff(@PathVariable("cutoff") String cutoff, @RequestHeader("X-Auth-Username") String username) {
        return ResponseEntity.ok("HELLO!");
    }
}
