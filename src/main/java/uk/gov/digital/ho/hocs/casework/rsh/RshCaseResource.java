package uk.gov.digital.ho.hocs.casework.rsh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.CaseSaveResponse;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseDetails;
import uk.gov.digital.ho.hocs.casework.rsh.dto.RshCaseCreateRequest;

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
    public ResponseEntity<CaseSaveResponse> rshCreateCase(@RequestBody RshCaseCreateRequest request, @RequestHeader("X-Auth-Username") String username) {
        CaseDetails caseDetails = rshCaseService.createRshCase(request.getCaseData(), request.getNotifyRequest(), username);
        if (caseDetails != null) {
            return ResponseEntity.ok(CaseSaveResponse.from(caseDetails));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/rsh/case/{caseUUID}", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CaseSaveResponse> rshUpdateCase(@PathVariable UUID caseUUID, @RequestBody RshCaseCreateRequest request, @RequestHeader("X-Auth-Username") String username) {
        CaseDetails caseDetails = rshCaseService.updateRshCase(caseUUID, request.getCaseData(), request.getNotifyRequest(), username);
        if (caseDetails != null) {
            return ResponseEntity.ok(CaseSaveResponse.from(caseDetails));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/rsh/case/{caseUUID}", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CaseDetails> rshGetCase(@PathVariable UUID caseUUID, @RequestHeader("X-Auth-Username") String username) {
        CaseDetails caseDetails = rshCaseService.getRSHCase(caseUUID,username);
        if (caseDetails != null) {
            return ResponseEntity.ok(caseDetails);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}