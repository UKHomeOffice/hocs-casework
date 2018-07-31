package uk.gov.digital.ho.hocs.casework.rsh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.rsh.dto.CreateRshCaseRequest;
import uk.gov.digital.ho.hocs.casework.rsh.dto.CreateRshCaseResponse;

import java.io.IOException;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController

class RshCaseResource {

    private final RshCaseService rshCaseService;

    @Autowired
    public RshCaseResource(RshCaseService rshCaseService) {

        this.rshCaseService = rshCaseService;
    }

    @RequestMapping(value = "/rsh/case", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateRshCaseResponse> rshCreateCase(@RequestBody CreateRshCaseRequest request) {
        try {
            CaseData caseData = rshCaseService.createRshCase(request.getCaseData(), request.getSendEmailRequest());
            return ResponseEntity.ok(CreateRshCaseResponse.from(caseData));
        } catch (EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/rsh/case/{caseUUID}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateRshCaseResponse> rshUpdateCase(@PathVariable UUID caseUUID, @RequestBody CreateRshCaseRequest request) {
        try {
            CaseData caseData = rshCaseService.updateRshCase(caseUUID, request.getCaseData(), request.getSendEmailRequest());
            return ResponseEntity.ok(CreateRshCaseResponse.from(caseData));
        } catch (EntityCreationException | EntityNotFoundException | IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/rsh/case/{caseUUID}", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CaseData> rshGetCase(@PathVariable UUID caseUUID) {
        try {
            CaseData caseData = rshCaseService.getRSHCase(caseUUID);
            return ResponseEntity.ok(caseData);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}