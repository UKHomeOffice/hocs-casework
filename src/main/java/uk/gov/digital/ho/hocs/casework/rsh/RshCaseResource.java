package uk.gov.digital.ho.hocs.casework.rsh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.rsh.dto.CreateRshCaseRequest;

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
    public ResponseEntity<CreateCaseResponse> rshCreateCase(@RequestBody CreateRshCaseRequest request, @RequestHeader("X-Auth-Username") String username) {
        try {
            CaseData caseData = rshCaseService.createRshCase(request.getCaseData(), request.getSendEmailRequest(), username);
            return ResponseEntity.ok(CreateCaseResponse.from(caseData));
        } catch (EntityCreationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/rsh/case/{caseUUID}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateCaseResponse> rshUpdateCase(@PathVariable UUID caseUUID, @RequestBody CreateRshCaseRequest request, @RequestHeader("X-Auth-Username") String username) {
        try {
            CaseData caseData = rshCaseService.updateRshCase(caseUUID, request.getCaseData(), request.getSendEmailRequest(), username);
            return ResponseEntity.ok(CreateCaseResponse.from(caseData));
        } catch (EntityCreationException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/rsh/case/{caseUUID}", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CaseData> rshGetCase(@PathVariable UUID caseUUID, @RequestHeader("X-Auth-Username") String username) {
        try {
            CaseData caseData = rshCaseService.getRSHCase(caseUUID, username);
            return ResponseEntity.ok(caseData);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}