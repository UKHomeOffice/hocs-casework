package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetInputDataResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetPrimaryTopicResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.model.InputData;

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

    @GetMapping(value = "/case/{caseUUID}/input")
    public ResponseEntity<GetInputDataResponse> getInputData(@PathVariable UUID caseUUID) {
        InputData inputData = inputDataService.getInputData(caseUUID);
        return ResponseEntity.ok(GetInputDataResponse.from(inputData));
    }

    @GetMapping(value = "/case/{caseUUID}/topiclist", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetPrimaryTopicResponse> getPrimaryTopicForCase(@PathVariable UUID caseUUID) {
        InputData inputData = inputDataService.getInputData(caseUUID);
        return ResponseEntity.ok(GetPrimaryTopicResponse.from(inputData));
    }
}