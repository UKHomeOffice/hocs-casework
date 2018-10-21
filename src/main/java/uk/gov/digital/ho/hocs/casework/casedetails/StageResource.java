package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.AllocateStageRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateStageRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateStageResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.StageDto;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Stage;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/case/{caseUUID}")
class StageResource {

    private final StageDataService stageDataService;

    @Autowired
    public StageResource(StageDataService stageDataService) {
        this.stageDataService = stageDataService;
    }

    @PostMapping(value = "/stage")
    public ResponseEntity<CreateStageResponse> createStage(@PathVariable UUID caseUUID, @RequestBody CreateStageRequest request) {
        Stage stage = stageDataService.createStage(caseUUID, request.getType(), request.getTeamUUID(), request.getUserUUID());
        return ResponseEntity.ok(CreateStageResponse.from(stage));
    }

    @PostMapping(value = "/stage/{stageUUID}/allocate")
    public ResponseEntity allocateStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody AllocateStageRequest allocateStageRequest) {
        stageDataService.allocateStage(caseUUID, stageUUID, allocateStageRequest.getTeamUUID(), allocateStageRequest.getUserUUID());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/stage/{stageUUID}/complete")
    public ResponseEntity completeStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        stageDataService.completeStage(caseUUID, stageUUID);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/stage/{stageUUID}")
    public ResponseEntity<StageDto> getStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        Stage stage = stageDataService.getStage(caseUUID, stageUUID);
        return ResponseEntity.ok(StageDto.from(stage));
    }

}