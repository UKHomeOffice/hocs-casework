package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.Authorised;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
class StageResource {

    private final StageService stageService;

    @Autowired
    public StageResource(StageService stageService) {
        this.stageService = stageService;
    }

    @Authorised(accessLevel = AccessLevel.WRITE)
    @PostMapping(value = "/case/{caseUUID}/stage")
    ResponseEntity<CreateStageResponse> createStage(@PathVariable UUID caseUUID, @RequestBody CreateStageRequest request) {
        Stage stage = stageService.createStage(caseUUID, request.getType(), request.getTeamUUID(), request.getDeadline(), request.getAllocationType(), request.getTransitionNoteUUID());
        return ResponseEntity.ok(CreateStageResponse.from(stage));
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}/stage/{stageUUID}")
    ResponseEntity<GetStageResponse> getStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        Stage stage = stageService.getActiveStage(caseUUID, stageUUID);
        return ResponseEntity.ok(GetStageResponse.from(stage));
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/user")
    ResponseEntity updateStageUser(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UpdateStageUserRequest request) {
        stageService.updateStageUser(caseUUID, stageUUID, request.getUserUUID());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/case/{caseUUID}/stage/{stageUUID}/user")
    ResponseEntity<UUID> getStageUser(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        UUID userUUID = stageService.getStageUser(caseUUID, stageUUID);
        return ResponseEntity.ok(userUUID);
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/team")
    ResponseEntity updateStageTeam(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UpdateStageTeamRequest request) {
        stageService.updateStageTeam(caseUUID, stageUUID, request.getTeamUUID(), request.getAllocationType());
        return ResponseEntity.ok().build();
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/transitionNote")
    ResponseEntity updateStageTransitionNote(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UpdateStageTeamRequest request) {
        stageService.updateStageTeam(caseUUID, stageUUID, request.getTeamUUID(), request.getAllocationType());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/case/{caseUUID}/stage/{stageUUID}/team")
    ResponseEntity<UUID> getStageTeam(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        UUID teamUUID = stageService.getStageTeam(caseUUID, stageUUID);
        return ResponseEntity.ok(teamUUID);
    }

    @GetMapping(value = "/stage/team/{teamUUID}")
    ResponseEntity<GetStagesResponse> getActiveStagesByTeamUUID(@PathVariable UUID teamUUID) {
        Set<Stage> activeStages = stageService.getActiveStagesByTeamUUID(teamUUID);
        return ResponseEntity.ok(GetStagesResponse.from(activeStages));
    }

    @GetMapping(value = "/stage")
    ResponseEntity<GetStagesResponse> getActiveStages() {
        Set<Stage> activeStages = stageService.getActiveStagesForUser();
        return ResponseEntity.ok(GetStagesResponse.from(activeStages));
    }

    @GetMapping(value = "/case/{reference:[a-zA-Z]{2,}%2F[0-9]{7}%2F[0-9]{2}}/stage")
    ResponseEntity<GetStagesResponse> getActiveStagesForCase(@PathVariable String reference) throws UnsupportedEncodingException {
        String decodedRef = URLDecoder.decode(reference, StandardCharsets.UTF_8.name());
        Set<Stage> activeStages = stageService.getActiveStagesByCaseReference(decodedRef);
        return ResponseEntity.ok(GetStagesResponse.from(activeStages));
    }

    @GetMapping(value = "/stage/team/{teamUUID}/user/{userUUID}")
    ResponseEntity<Set<UUID>> getActiveStageCaseUUIDsForUserAndTeam(@PathVariable UUID userUUID, @PathVariable UUID teamUUID){
        Set<UUID> caseUUIDs = stageService.getActiveStageCaseUUIDsForUserAndTeam(userUUID, teamUUID);
        return ResponseEntity.ok(caseUUIDs);
    }
}