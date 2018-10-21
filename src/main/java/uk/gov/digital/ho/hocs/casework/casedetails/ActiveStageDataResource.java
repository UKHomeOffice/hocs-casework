package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetActiveStagesResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Stage;

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
        //Set<Stage> activeStages = stageDataService.getActiveStages();
        // return ResponseEntity.ok(GetActiveStagesResponse.from(activeStages));
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/active/user/{userUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetActiveStagesResponse> getActiveStagesByUserUUID(@PathVariable UUID userUUID) {
        Set<Stage> activeStages = stageDataService.getActiveStagesByUserUUID(userUUID);
        return ResponseEntity.ok(GetActiveStagesResponse.from(activeStages));
    }

    @GetMapping(value = "/active/team/{teamUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetActiveStagesResponse> getActiveStagesByTeamUUIDs(@PathVariable UUID teamUUID) {
        Set<Stage> activeStages = stageDataService.getActiveStagesByTeamUUID(teamUUID);
        return ResponseEntity.ok(GetActiveStagesResponse.from(activeStages));
    }
}