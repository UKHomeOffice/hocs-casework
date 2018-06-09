package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
class CaseResource {

    private final CaseService caseService;



    @Autowired
    public CaseResource(CaseService caseService) {
        this.caseService = caseService;
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
