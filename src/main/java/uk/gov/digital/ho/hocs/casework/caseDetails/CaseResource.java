package uk.gov.digital.ho.hocs.casework.caseDetails;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.model.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
public class CaseResource {

    private final CaseService caseService;

    private CsvMapper csvMapper;


    @Autowired
    public CaseResource(CaseService caseService) {
        this.caseService = caseService;
        this.csvMapper = new CsvMapper();
        HocsCaseServiceConfiguration.initialiseObjectMapper(this.csvMapper);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CaseSaveResponse> createCase(@RequestBody CaseCreateRequest request, @RequestHeader("X-Auth-Username") String username) {
        CaseDetails caseDetails = caseService.createCase(request.getCaseType(), username);
        return ResponseEntity.ok(CaseSaveResponse.from(caseDetails));
    }

    @RequestMapping(value = "/case/{uuid}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity updateCase(@PathVariable UUID caseUUID, @RequestBody UpdateStageRequest request, @RequestHeader("X-Auth-Username") String username) {
        caseService.updateStage(request.getStageUUID(),  request.getSchemaVersion(), request.getStageData(), username);
        return ResponseEntity.ok().build();
    }
}
