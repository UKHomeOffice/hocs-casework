package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.UpdateStageRequest;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
class InputDataResource {

    private final InputDataService inputDataService;

    @Autowired
    public InputDataResource(InputDataService inputDataService) {
        this.inputDataService = inputDataService;
    }

    @PostMapping(value = "/case/{caseUUID}/stage/{stageUUID}", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity updateStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UpdateStageRequest request) {
        inputDataService.updateStage(caseUUID, stageUUID, request.getData());
        return ResponseEntity.ok().build();
    }
}