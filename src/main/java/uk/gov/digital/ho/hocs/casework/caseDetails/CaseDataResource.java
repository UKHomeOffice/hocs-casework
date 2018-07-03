package uk.gov.digital.ho.hocs.casework.caseDetails;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
class CaseDataResource {

    private final CaseDataService caseDataService;

    @Autowired
    public CaseDataResource(CaseDataService caseDataService) {
        this.caseDataService = caseDataService;
    }

    @RequestMapping(value = "/case", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateCaseResponse> createCase(@RequestBody CreateCaseRequest request) {
        try {
            CaseData caseData = caseDataService.createCase(request.getCaseType());
            return ResponseEntity.ok(CreateCaseResponse.from(caseData));
        } catch (EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/case/{caseUUID}", method = RequestMethod.PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity updateCase(@PathVariable UUID caseUUID, @RequestBody UpdateCaseRequest request) {
        try {
            caseDataService.updateCase(caseUUID, request.getCaseType());
            return ResponseEntity.ok().build();
        } catch (EntityCreationException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/case/{caseUUID}/stage", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateStageResponse> createStage(@PathVariable UUID caseUUID, @RequestBody CreateStageRequest request) {
        try {
            StageData stageData = caseDataService.createStage(caseUUID, request.getStageType(), request.getStageData());
            return ResponseEntity.ok(CreateStageResponse.from(stageData));
        } catch (EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/case/{caseUUID}/stage/{stageUuid}", method = RequestMethod.PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity updateStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUuid, @RequestBody UpdateStageRequest request) {
        try {
            caseDataService.updateStage(caseUUID, stageUuid, request.getStageType(), request.getStageData());
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException | EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
