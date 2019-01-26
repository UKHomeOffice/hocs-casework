package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseSummary;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.Allocated;
import uk.gov.digital.ho.hocs.casework.security.AllocationLevel;
import uk.gov.digital.ho.hocs.casework.security.Authorised;

import java.util.UUID;

@Slf4j
@RestController
class CaseDataResource {

    private final CaseDataService caseDataService;

    @Autowired
    public CaseDataResource(CaseDataService caseDataService) {
        this.caseDataService = caseDataService;
    }

    @Authorised(accessLevel = AccessLevel.WRITE)
    @PostMapping(value = "/case")
    ResponseEntity<CreateCaseResponse> createCase(@RequestBody CreateCaseRequest request) {
        CaseData caseData = caseDataService.createCase(request.getType(), request.getData(), request.getCaseDeadline(), request.getDateReceieved());
        return ResponseEntity.ok(CreateCaseResponse.from(caseData));
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}")
    ResponseEntity<GetCaseResponse> getCase(@PathVariable UUID caseUUID) {
        CaseData caseData = caseDataService.getCase(caseUUID);
        return ResponseEntity.ok(GetCaseResponse.from(caseData));
    }

    @Authorised(accessLevel = AccessLevel.OWNER)
    @DeleteMapping(value = "/case/{caseUUID}")
    ResponseEntity deleteCase(@PathVariable UUID caseUUID) {
        caseDataService.deleteCase(caseUUID);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/case/{caseUUID}/type")
    ResponseEntity<String> getCaseType(@PathVariable UUID caseUUID) {
        String caseDataType = caseDataService.getCaseType(caseUUID);
        return ResponseEntity.ok(caseDataType);
    }

    @Authorised(accessLevel = AccessLevel.SUMMARY)
    @GetMapping(value = "/case/{caseUUID}/summary")
    ResponseEntity<GetCaseSummaryResponse> getCaseSummary(@PathVariable UUID caseUUID) {
        CaseSummary caseSummary = caseDataService.getCaseSummary(caseUUID);
        return ResponseEntity.ok(GetCaseSummaryResponse.from(caseSummary));
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}/timeline")
    ResponseEntity getCaseTimeline(@PathVariable UUID caseUUID) {
        caseDataService.getCaseTimeline(caseUUID);
        return ResponseEntity.ok().build();
    }

    @Allocated(allocatedTo = AllocationLevel.USER)
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/data")
    ResponseEntity updateCaseData(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UpdateCaseDataRequest request) {
        caseDataService.updateCaseData(caseUUID, stageUUID, request.getData());
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/primaryCorrespondent")
    ResponseEntity updateCasePrimaryCorrespondent(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UUID primaryCorrespondentUUID) {
        caseDataService.updatePrimaryCorrespondent(caseUUID, stageUUID, primaryCorrespondentUUID);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/primaryTopic")
    ResponseEntity updateCasePrimaryTopic(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UUID primaryTopicUUID) {
        caseDataService.updatePrimaryTopic(caseUUID, stageUUID, primaryTopicUUID);
        return ResponseEntity.ok().build();
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}/standardLine")
    ResponseEntity<GetStandardLineResponse> getStandardLine(@PathVariable UUID caseUUID) {
        GetStandardLineResponse standardLine = caseDataService.getStandardLine(caseUUID);
        return ResponseEntity.ok(standardLine);
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}/template")
    public ResponseEntity<GetTemplateResponse> getTemplate(@PathVariable UUID caseUUID) {
        GetTemplateResponse template =  caseDataService.getTemplate(caseUUID);
        return ResponseEntity.ok(template);
    }
}
