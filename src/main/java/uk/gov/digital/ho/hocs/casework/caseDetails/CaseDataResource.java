package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.UpdateStageRequest;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
class CaseDataResource {

    private final CaseDataService caseDataService;



    @Autowired
    public CaseDataResource(CaseDataService caseDataService) {
        this.caseDataService = caseDataService;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateCaseResponse> createCase(@RequestBody CreateCaseRequest request, @RequestHeader("X-Auth-Username") String username) {
        CaseData caseData = caseDataService.createCase(request.getCaseType(), username);
        return ResponseEntity.ok(CreateCaseResponse.from(caseData));
    }

    @RequestMapping(value = "/case/{uuid}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity updateCase(@PathVariable UUID caseUUID, @RequestBody UpdateStageRequest request, @RequestHeader("X-Auth-Username") String username) {
        caseDataService.updateStage(request.getStageUUID(), request.getSchemaVersion(), request.getStageData(), username);
        return ResponseEntity.ok().build();
    }
}
