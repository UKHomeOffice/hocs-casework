package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.casedetails.queuedto.CreateCorrespondentRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.queuedto.CreateReferenceRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.queuedto.UpdateDeadlinesRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.queuedto.UpdateInputDataRequest;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
class DevOnlyResource {

    private final InputDataService inputDataService;
    private final ReferenceDataService referenceDataService;
    private final DeadlineDataService deadlineDataService;
    private final CorrespondentDataService correspondentDataService;

    @Autowired
    public DevOnlyResource(InputDataService inputDataService, ReferenceDataService referenceDataService, DeadlineDataService deadlineDataService, CorrespondentDataService correspondentDataService) {
        this.inputDataService = inputDataService;
        this.referenceDataService = referenceDataService;
        this.deadlineDataService = deadlineDataService;
        this.correspondentDataService = correspondentDataService;

    }

    @PostMapping(value = "/input")
    public ResponseEntity updateInputData(@RequestBody UpdateInputDataRequest request) {
        inputDataService.setInputData(request.getCaseUUID(), request.getData());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/reference")
    public ResponseEntity recordReference(@RequestBody CreateReferenceRequest request) {
        referenceDataService.createReference(request.getCaseUUID(), request.getReference(), request.getType());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/deadline")
    public ResponseEntity storeDeadlines(@RequestBody UpdateDeadlinesRequest request, @PathVariable UUID caseUUID) {
        deadlineDataService.updateDeadlines(caseUUID, request.getDeadlines());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/correspondent", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity addCorrespondentToCase(@RequestBody CreateCorrespondentRequest request, @PathVariable UUID caseUUID) {
        correspondentDataService.createCorrespondent(caseUUID, request.getFullname(), request.getPostcode(), request.getAddress1(), request.getAddress2(), request.getAddress3(), request.getCountry(), request.getTelephone(), request.getEmail(), request.getType());
        return ResponseEntity.ok().build();
    }

}