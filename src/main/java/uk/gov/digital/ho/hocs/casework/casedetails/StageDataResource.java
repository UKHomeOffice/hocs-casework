package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.AllocateStageRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateStageRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateStageResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetStageResponse;
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
        StageData stageData = stageDataService.createStage(caseUUID, request.getType(), request.getTeamUUID(), request.getUserUUID());
        return ResponseEntity.ok(CreateStageResponse.from(stageData));
    }

    @PostMapping(value = "/case/{caseUUID}/stage/{stageUUID}/allocate")
    public ResponseEntity allocateStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody AllocateStageRequest allocateStageRequest) {
        stageDataService.allocateStage(stageUUID, allocateStageRequest.getTeamUUID(), allocateStageRequest.getUserUUID());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/case/{caseUUID}/stage/{stageUUID}")
    public ResponseEntity<GetStageResponse> getStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        StageData stageData = stageDataService.getStage(stageUUID);
        return ResponseEntity.ok(GetStageResponse.from(stageData));
    }

}