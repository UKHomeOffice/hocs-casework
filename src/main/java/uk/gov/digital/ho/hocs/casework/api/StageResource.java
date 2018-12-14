package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.Authorised;

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

    @Authorised(accessLevel = AccessLevel.WRITE)
    @PostMapping(value = "/case/{caseUUID}/stage", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateStageResponse> createStage(@PathVariable UUID caseUUID, @RequestBody CreateStageRequest request) {
        Stage stage = stageService.createStage(caseUUID, request.getType(), request.getTeamUUID(), request.getDeadline(), request.getAllocationType());
        return ResponseEntity.ok(CreateStageResponse.from(stage));
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}/stage/{stageUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<StageDto> getStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        Stage stage = stageService.getStage(caseUUID, stageUUID);
        return ResponseEntity.ok(StageDto.from(stage));
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/user", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity updateStageUser(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UpdateStageUserRequest request) {
        stageService.updateStageUser(caseUUID, stageUUID, request.getUserUUID());
        return ResponseEntity.ok().build();
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/team", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity updateStageTeam(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UpdateStageTeamRequest request) {
        stageService.updateStageTeam(caseUUID, stageUUID, request.getTeamUUID(), request.getAllocationType());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/case/{caseUUID}/stage/{stageUUID}/user", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UUID> getStageUser(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        UUID user = stageService.getStageUser(caseUUID, stageUUID);
        return ResponseEntity.ok(user);
    }

    @GetMapping(value = "/case/{caseUUID}/stage/{stageUUID}/team", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UUID> getStageTeam(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        UUID team = stageService.getStageTeam(caseUUID, stageUUID);
        return ResponseEntity.ok(team);
    }

    @GetMapping(value = "/stage", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetStagesResponse> getActiveStages() {
        Set<Stage> activeStages = stageService.getActiveStagesForUser();
        return ResponseEntity.ok(GetStagesResponse.from(activeStages));
    }

}