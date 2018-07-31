package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateStageRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateStageResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetStageResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.UpdateStageRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;

import java.io.IOException;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
class StageDataResource {

    private final StageDataService stageDataService;

    @Autowired
    public StageDataResource(StageDataService stageDataService) {
        this.stageDataService = stageDataService;
    }

    @RequestMapping(value = "/case/{caseUUID}/stage", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateStageResponse> createStage(@PathVariable UUID caseUUID, @RequestBody CreateStageRequest request) {
        try {
            StageData stageData = stageDataService.createStage(caseUUID, request.getType(), request.getData());
            return ResponseEntity.ok(CreateStageResponse.from(stageData));
        } catch (EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/case/{caseUUID}/stage/{stageUUID}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity createScreen(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UpdateStageRequest request) {
        try {
            stageDataService.updateStage(caseUUID, stageUUID, request.getData());
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException | EntityCreationException | IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    //TODO: this needs to change to happen when it is assigned to the next user.
    @RequestMapping(value = "/case/{caseUUID}/stage/{stageUUID}/allocate", method = RequestMethod.GET)
    public ResponseEntity allocateStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        try {
            stageDataService.allocateStage(caseUUID, stageUUID);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException | EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    //TODO: this needs to change to happen when it is assigned to the next user.
    @RequestMapping(value = "/case/{caseUUID}/stage/{stageUUID}/complete", method = RequestMethod.GET)
    public ResponseEntity completeStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        try {
            stageDataService.completeStage(caseUUID, stageUUID);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException | EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/case/{caseUUID}/stage/{stageUUID}", method = RequestMethod.GET)
    public ResponseEntity<GetStageResponse> getStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        try {
            StageData stageData = stageDataService.getStage(caseUUID, stageUUID);
            return ResponseEntity.ok(GetStageResponse.from(stageData));
        } catch (EntityNotFoundException | EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
