package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetActiveStagesRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetActiveStagesResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ActiveStage;

import java.util.Set;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
class ActiveStageResource {

    private final ActiveStageService activeStageService;

    @Autowired
    public ActiveStageResource(ActiveStageService activeStageService) {
        this.activeStageService = activeStageService;
    }

    @GetMapping(value = "/stage/active", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetActiveStagesResponse> getActiveStages() {
        Set<ActiveStage> activeStages = activeStageService.getActiveCases();
        return ResponseEntity.ok(GetActiveStagesResponse.from(activeStages));
    }

    @GetMapping(value = "/stage/active/{userUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetActiveStagesResponse> getActiveStagesByUserUUID(@PathVariable UUID userUUID) {
        Set<ActiveStage> activeStages = activeStageService.getActiveStagesByUserUUID(userUUID);
        return ResponseEntity.ok(GetActiveStagesResponse.from(activeStages));
    }

    @PostMapping(value = "/stage/active/team/", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetActiveStagesResponse> getActiveStagesByTeamUUIDs(@RequestBody GetActiveStagesRequest request) {
        Set<ActiveStage> activeStages = activeStageService.getActiveStagesByTeamUUID(request.getTeams());
        return ResponseEntity.ok(GetActiveStagesResponse.from(activeStages));
    }
}