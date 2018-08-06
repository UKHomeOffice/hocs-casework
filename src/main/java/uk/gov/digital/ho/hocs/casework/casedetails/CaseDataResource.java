package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetCaseResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.UpdateCaseRequest;
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

    @PostMapping(value = "/case", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateCaseResponse> createCase(@RequestBody CreateCaseRequest request) {
        CaseData caseData = caseDataService.createCase(request.getType());
        return ResponseEntity.ok(CreateCaseResponse.from(caseData));
    }

    @GetMapping(value = "/case/{caseUUID}")
    public ResponseEntity<GetCaseResponse> getCase(@PathVariable UUID caseUUID) {
        CaseData caseData = caseDataService.getCase(caseUUID);
        return ResponseEntity.ok(GetCaseResponse.from(caseData));
    }

    @PostMapping(value = "/case/{caseUUID}", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity updateCase(@PathVariable UUID caseUUID, @RequestBody UpdateCaseRequest request) {
        caseDataService.updateCase(caseUUID);
        return ResponseEntity.ok().build();
    }

}