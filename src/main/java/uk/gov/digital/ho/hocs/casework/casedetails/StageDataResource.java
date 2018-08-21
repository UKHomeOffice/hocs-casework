package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.*;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;

import java.util.Set;
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

    @PostMapping(value = "/case/{caseUUID}/stage")
    public ResponseEntity<CreateStageResponse> createStage(@PathVariable UUID caseUUID, @RequestBody CreateStageRequest request) {
        StageData stageData = stageDataService.createStage(caseUUID, request.getType(), request.getTeamUUID(), request.getUserUUID());
        return ResponseEntity.ok(CreateStageResponse.from(stageData));
    }

    @PostMapping(value = "/case/{caseUUID}/stage/{stageUUID}/allocate")
    public ResponseEntity allocateStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody AllocateStageRequest allocateStageRequest) {
        stageDataService.allocateStage(stageUUID, allocateStageRequest.getTeamUUID(), allocateStageRequest.getUserUUID());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/case/{caseUUID}/stage/{stageUUID}/complete")
    public ResponseEntity completeStage(@PathVariable UUID stageUUID) {
        stageDataService.completeStage(stageUUID);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/case/{caseUUID}/stage/{stageUUID}")
    public ResponseEntity<GetStageResponse> getStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        StageData stageData = stageDataService.getStage(stageUUID);
        return ResponseEntity.ok(GetStageResponse.from(stageData));
    }

    @GetMapping(value = "/stage/active", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetActiveStagesResponse> getActiveStages() {
        Set<StageData> activeStages = stageDataService.getActiveStages();
        return ResponseEntity.ok(GetActiveStagesResponse.from(activeStages));
    }

    @GetMapping(value = "/stage/active/{userUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetActiveStagesResponse> getActiveStagesByUserUUID(@PathVariable UUID userUUID) {
        Set<StageData> activeStages = stageDataService.getActiveStagesByUserUUID(userUUID);
        return ResponseEntity.ok(GetActiveStagesResponse.from(activeStages));
    }

    @PostMapping(value = "/stage/active/team/", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetActiveStagesResponse> getActiveStagesByTeamUUIDs(@RequestBody GetActiveStagesRequest request) {
        Set<StageData> activeStages = stageDataService.getActiveStagesByTeamUUID(request.getTeams());
        return ResponseEntity.ok(GetActiveStagesResponse.from(activeStages));
    }
}