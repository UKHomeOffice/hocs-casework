package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ActiveStage;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
class ActiveStageResource {

    private final ActiveStageService activeStageService;

    @Autowired
    public ActiveStageResource(ActiveStageService activeStageService) {
        this.activeStageService = activeStageService;
    }

    @RequestMapping(value = "/case/active", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Iterable<ActiveStage>> getActiveCases() {
        try {
            Iterable<ActiveStage> activeStages = activeStageService.getActiveCases();
            return ResponseEntity.ok(activeStages);
        } catch (EntityNotFoundException | EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
