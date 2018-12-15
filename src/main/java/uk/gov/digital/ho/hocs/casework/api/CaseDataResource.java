package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.Allocated;
import uk.gov.digital.ho.hocs.casework.security.AllocationLevel;
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

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    ResponseEntity<GetCaseDataResponse> getCase(@PathVariable UUID caseUUID) {
        CaseData caseData = caseDataService.getCase(caseUUID);
        return ResponseEntity.ok(GetCaseDataResponse.from(caseData));
    }

    @Authorised(accessLevel = AccessLevel.SUMMARY)
    @GetMapping(value = "/case/{caseUUID}/summary", produces = APPLICATION_JSON_UTF8_VALUE)
    ResponseEntity<GetCaseSummaryResponse> getCaseSummary(@PathVariable UUID caseUUID) throws IOException {
        GetCaseSummaryResponse caseData = caseDataService.getCaseSummary(caseUUID);
        return ResponseEntity.ok(caseData);
    }

    @GetMapping(value = "/case/{caseUUID}/type", consumes = APPLICATION_JSON_UTF8_VALUE)
    ResponseEntity<String> getCaseType(@PathVariable UUID caseUUID) {
        String caseDataType = caseDataService.getCaseType(caseUUID);
        return ResponseEntity.ok(caseDataType);
    }

    @Authorised(accessLevel = AccessLevel.WRITE)
    @PostMapping(value = "/case", consumes = APPLICATION_JSON_UTF8_VALUE)
    ResponseEntity<CreateCaseResponse> createCase(@RequestBody CreateCaseRequest request) {
        CaseData caseData = caseDataService.createCase(request.getType(), request.getData(), request.getCaseDeadline(), request.getDateReceieved());
        return ResponseEntity.ok(CreateCaseResponse.from(caseData));
    }

    @Allocated(allocatedTo = AllocationLevel.USER)
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/data", consumes = APPLICATION_JSON_UTF8_VALUE)
    ResponseEntity updateCaseData(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UpdateCaseDataRequest request) {
        caseDataService.updateCaseData(caseUUID, stageUUID, request.getData());
        return ResponseEntity.ok().build();
    }

    @Authorised(accessLevel = AccessLevel.OWNER)
    @DeleteMapping(value = "/case/{caseUUID}", consumes = APPLICATION_JSON_UTF8_VALUE)
    ResponseEntity deleteCase(@PathVariable UUID caseUUID) {
        caseDataService.deleteCase(caseUUID);
        return ResponseEntity.ok().build();
    }
}
