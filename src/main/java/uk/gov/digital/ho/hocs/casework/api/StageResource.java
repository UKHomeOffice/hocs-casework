package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.util.Set;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
class StageResource {

    private final StageService stageService;

    @Autowired
    public StageResource(StageService stageService) {
        this.stageService = stageService;
    }

    @PostMapping(value = "/case/{caseUUID}/stage")
    public ResponseEntity<CreateStageResponse> createStage(@PathVariable UUID caseUUID, @RequestBody CreateStageRequest request) {
        Stage stage = stageService.createStage(caseUUID, request.getType(), request.getTeamUUID(), request.getUserUUID(), request.getStatus());
        return ResponseEntity.ok(CreateStageResponse.from(stage));
    }

    @GetMapping(value = "/case/{caseUUID}/stage/{stageUUID}")
    public ResponseEntity<StageDto> getStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        Stage stage = stageService.getStage(caseUUID, stageUUID);
        return ResponseEntity.ok(StageDto.from(stage));
    }

    @PatchMapping(value = "/case/{caseUUID}/stage/{stageUUID}")
    public ResponseEntity updateStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UpdateStageRequest updateStageRequest) {
        stageService.updateStage(caseUUID, stageUUID, updateStageRequest.getTeamUUID(), updateStageRequest.getUserUUID(), updateStageRequest.getStatus());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/stage/owner/user/{userUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetStagesResponse> getActiveStagesByUserUUID(@PathVariable UUID userUUID) {
        Set<Stage> activeStages = stageService.getActiveStagesByUserUUID(userUUID);
        return ResponseEntity.ok(GetStagesResponse.from(activeStages));
    }

    @GetMapping(value = "/stage/owner/team/{teamUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetStagesResponse> getActiveStagesByTeamUUID(@PathVariable UUID teamUUID) {
        Set<Stage> activeStages = stageService.getActiveStagesByTeamUUID(teamUUID);
        return ResponseEntity.ok(GetStagesResponse.from(activeStages));
    }

    /*
    Temp code as call should move to workflow or somewhere that can work out the teams.
     */
    @GetMapping(value = "/stage/active", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetStagesResponse> getActiveStages() {
        Set<Stage> activeStages = stageService.getActiveStages();
        return ResponseEntity.ok(GetStagesResponse.from(activeStages));
    }

}