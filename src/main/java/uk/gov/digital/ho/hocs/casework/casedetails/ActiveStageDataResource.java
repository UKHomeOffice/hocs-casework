package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetActiveStagesRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetActiveStagesResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;

import java.util.Set;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/stage")
class ActiveStageDataResource {

    private final StageDataService stageDataService;

    @Autowired
    public ActiveStageDataResource(StageDataService stageDataService) {
        this.stageDataService = stageDataService;
    }

    @GetMapping(value = "/active", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetActiveStagesResponse> getActiveStages() {
        Set<StageData> activeStages = stageDataService.getActiveStages();
        return ResponseEntity.ok(GetActiveStagesResponse.from(activeStages));
    }

    @GetMapping(value = "/active/{userUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetActiveStagesResponse> getActiveStagesByUserUUID(@PathVariable UUID userUUID) {
        Set<StageData> activeStages = stageDataService.getActiveStagesByUserUUID(userUUID);
        return ResponseEntity.ok(GetActiveStagesResponse.from(activeStages));
    }

    @PostMapping(value = "/active/team", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetActiveStagesResponse> getActiveStagesByTeamUUIDs(@RequestBody GetActiveStagesRequest request) {
        Set<StageData> activeStages = stageDataService.getActiveStagesByTeamUUID(request.getTeams());
        return ResponseEntity.ok(GetActiveStagesResponse.from(activeStages));
    }
}