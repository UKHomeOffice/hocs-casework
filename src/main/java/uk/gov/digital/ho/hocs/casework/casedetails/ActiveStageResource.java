package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetActiveStageResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ActiveStage;

import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
class ActiveStageResource {

    private final ActiveStageService activeStageService;

    @Autowired
    public ActiveStageResource(ActiveStageService activeStageService) {
        this.activeStageService = activeStageService;
    }

    @GetMapping(value = "/case/active", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetActiveStageResponse> getActiveCases() {
        Set<ActiveStage> activeStages = activeStageService.getActiveCases();
        return ResponseEntity.ok(GetActiveStageResponse.from(activeStages));
    }
}