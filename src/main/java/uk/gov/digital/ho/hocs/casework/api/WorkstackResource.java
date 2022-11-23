package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.api.dto.GetWorkstackResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetWorkstacksResponse;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.ActiveStage;

import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
class WorkstackResource {

    private final WorkstackService workstackService;

    public WorkstackResource(WorkstackService workstackService) {
        this.workstackService = workstackService;
    }

    @GetMapping(value = "/stage/team/{teamUUID}")
    ResponseEntity<GetWorkstacksResponse> getActiveStagesByTeamUUID(@PathVariable UUID teamUUID) {
        Set<ActiveStage> activeStages = workstackService.getActiveStagesByTeamUUID(teamUUID);
        return ResponseEntity.ok(GetWorkstacksResponse.from(activeStages));
    }

    @GetMapping(value = "/stage")
    ResponseEntity<GetWorkstacksResponse> getActiveStages() {
        Set<ActiveStage> activeStages = workstackService.getActiveStagesForUsersTeams();
        return ResponseEntity.ok(GetWorkstacksResponse.from(activeStages));
    }

    @GetMapping(value = "/stage/user/{userUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GetWorkstacksResponse> getActiveStagesForUser(@PathVariable UUID userUuid) {
        Set<ActiveStage> activeStages = workstackService.getActiveUserStagesWithTeamsForUser(userUuid);
        return ResponseEntity.ok(GetWorkstacksResponse.from(activeStages));
    }

    @PutMapping(value = "/case/team/{teamUUID}/allocate/user/next")
    ResponseEntity allocateStageUser(@PathVariable UUID teamUUID,
                                     @RequestHeader(RequestData.USER_ID_HEADER) UUID userUUID) {
        ActiveStage stage = workstackService.getUnassignedAndActiveStageByTeamUUID(teamUUID, userUUID);
        if (stage==null) {
            return ResponseEntity.ok(stage);
        }
        return ResponseEntity.ok(GetWorkstackResponse.from(stage));
    }

}

