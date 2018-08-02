package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetCaseResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.UpdateCaseRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
class CaseDataResource {

    private final CaseDataService caseDataService;

    @Autowired
    public CaseDataResource(CaseDataService caseDataService) {
        this.caseDataService = caseDataService;
    }

    @RequestMapping(value = "/case", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateCaseResponse> createCase(@RequestBody CreateCaseRequest request) {
        try {
            CaseData caseData = caseDataService.createCase(request.getType());
            return ResponseEntity.ok(CreateCaseResponse.from(caseData));
        } catch (EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/case/{caseUUID}", method = RequestMethod.PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity updateCase(@PathVariable UUID caseUUID, @RequestBody UpdateCaseRequest request) {
        try {
            caseDataService.updateCase(caseUUID);
            return ResponseEntity.ok().build();
        } catch (EntityCreationException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/case/{caseUUID}", method = RequestMethod.GET)
    public ResponseEntity<GetCaseResponse> getCase(@PathVariable UUID caseUUID) {
        try {
            CaseData caseData = caseDataService.getCase(caseUUID);
            return ResponseEntity.ok(GetCaseResponse.from(caseData));
        } catch (EntityCreationException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
