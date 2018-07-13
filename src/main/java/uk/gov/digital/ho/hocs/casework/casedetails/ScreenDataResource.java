package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateScreenRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
class ScreenDataResource {

    private final ScreenDataService screenDataService;

    @Autowired
    public ScreenDataResource(ScreenDataService screenDataService) {
        this.screenDataService = screenDataService;
    }


    @RequestMapping(value = "/case/{caseUUID}/stage/{stageUUID}/screen", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity createScreen(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody CreateScreenRequest request) {
        try {
            screenDataService.createScreenForStage(stageUUID, request.getName(), request.getData());
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException | EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
