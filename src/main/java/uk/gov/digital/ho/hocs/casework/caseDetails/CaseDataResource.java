package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.*;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageData;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
class CaseDataResource {

    private final CaseDataService caseDataService;


    @Autowired
    public CaseDataResource(CaseDataService caseDataService) {
        this.caseDataService = caseDataService;
    }

    @RequestMapping(value = "/case", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateCaseResponse> createCase(@RequestBody CreateCaseRequest request, @RequestHeader("X-Auth-Username") String username) {
        try {
            CaseData caseData = caseDataService.createCase(request.getCaseType(), username);
            return ResponseEntity.ok(CreateCaseResponse.from(caseData));
        } catch (EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/case/{caseUuid}", method = RequestMethod.PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdateCaseResponse> updateCase(@PathVariable UUID caseUuid, @RequestBody UpdateCaseRequest request, @RequestHeader("X-Auth-Username") String username) {
        try {
            CaseData caseData = caseDataService.updateCase(caseUuid, request.getCaseType(), username);
            return ResponseEntity.ok(UpdateCaseResponse.from(caseData));
        } catch (EntityCreationException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/case/{caseUuid}/stage", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateStageResponse> createStage(@PathVariable UUID caseUuid, @RequestBody CreateStageRequest request, @RequestHeader("X-Auth-Username") String username) {
        try {
            StageData stageData = caseDataService.createStage(caseUuid, request.getStageType(), request.getStageData(), username);
            return ResponseEntity.ok(CreateStageResponse.from(stageData));
        } catch (EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/case/{caseUuid}/stage/{stageUuid}", method = RequestMethod.PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdateStageResponse> updateStage(@PathVariable UUID caseUuid, @PathVariable UUID stageUuid, @RequestBody UpdateStageRequest request, @RequestHeader("X-Auth-Username") String username) {
        try {
            StageData stageData = caseDataService.updateStage(caseUuid, stageUuid, request.getStageType(), request.getStageData(), username);
            return ResponseEntity.ok(UpdateStageResponse.from(stageData));
        } catch (EntityNotFoundException | EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
