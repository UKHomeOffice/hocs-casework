package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.UpdateActiveStageRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
class ActiveStageDataResource {

    private final ActiveStageDataService activeStageDataService;

    @Autowired
    public ActiveStageDataResource(ActiveStageDataService activeStageDataService) {
        this.activeStageDataService = activeStageDataService;
    }


    @RequestMapping(value = "/case/{caseUUID}/stage/{stageUUID}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity createScreen(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UpdateActiveStageRequest request) {
        try {
            activeStageDataService.createScreenForStage(stageUUID, request.getName(), request.getData());
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException | EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
