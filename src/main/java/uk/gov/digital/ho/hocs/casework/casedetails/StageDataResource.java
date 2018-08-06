package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.*;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;

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

    @PostMapping(value = "/case/{caseUUID}/stage", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateStageResponse> createStage(@PathVariable UUID caseUUID, @RequestBody CreateStageRequest request) {
        StageData stageData = stageDataService.createStage(caseUUID, request.getType(), request.getTeamUUID(), request.getUserUUID(), request.getData());
        return ResponseEntity.ok(CreateStageResponse.from(stageData));
    }

    @GetMapping(value = "/case/{caseUUID}/stage/{stageUUID}")
    public ResponseEntity<GetStageResponse> getStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        StageData stageData = stageDataService.getStage(caseUUID, stageUUID);
        return ResponseEntity.ok(GetStageResponse.from(stageData));
    }

    @PostMapping(value = "/case/{caseUUID}/stage/{stageUUID}", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity updateStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UpdateStageRequest request) {
        stageDataService.updateStage(caseUUID, stageUUID, request.getData());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/case/{caseUUID}/stage/{stageUUID}/allocate")
    public ResponseEntity allocateStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody AllocateStageRequest allocateStageRequest) {
        stageDataService.allocateStage(caseUUID, stageUUID, allocateStageRequest.getTeamUUID(), allocateStageRequest.getUserUUID());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/case/{caseUUID}/stage/{stageUUID}/complete")
    public ResponseEntity completeStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        stageDataService.completeStage(caseUUID, stageUUID);
        return ResponseEntity.ok().build();
    }
}