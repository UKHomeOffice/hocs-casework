package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.UpdateInputDataRequest;

import java.util.UUID;

@Slf4j
@RestController
class InputDataResource {

    private final InputDataService inputDataService;

    @Autowired
    public InputDataResource(InputDataService inputDataService) {
        this.inputDataService = inputDataService;
    }

    @PostMapping(value = "/case/{caseUUID}/input")
    public ResponseEntity updateInputData(@PathVariable UUID caseUUID, @RequestBody UpdateInputDataRequest request) {
        inputDataService.updateInputData(caseUUID, request.getData());
        return ResponseEntity.ok().build();
    }
}