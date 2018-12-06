package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataDto;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.Authorised;

import java.io.IOException;
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

    @Authorised()
    @PostMapping(value = "/case", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CreateCaseResponse> createCase(@RequestBody CreateCaseRequest request) {
        CaseData caseData = caseDataService.createCase(request.getType(), request.getData(), request.getCaseDeadline(), request.getDateReceieved());
        return ResponseEntity.ok(CreateCaseResponse.from(caseData));
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CaseDataDto> getCase(@PathVariable UUID caseUUID) {
        CaseData caseData = caseDataService.getCase(caseUUID);
        return ResponseEntity.ok(CaseDataDto.from(caseData));
    }

    @Authorised(accessLevel = AccessLevel.OWNER)
    @DeleteMapping(value = "/case/{caseUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity deleteCase(@PathVariable UUID caseUUID) {
        caseDataService.deleteCase(caseUUID);
        return ResponseEntity.ok().build();
    }

    @Authorised(accessLevel = AccessLevel.SUMMARY)
    @GetMapping(value = "/case/{caseUUID}/summary", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CaseSummary> getCaseSummary(@PathVariable UUID caseUUID) throws IOException {
        CaseSummary caseData = caseDataService.getCaseSummary(caseUUID);
        return ResponseEntity.ok(caseData);
    }

}