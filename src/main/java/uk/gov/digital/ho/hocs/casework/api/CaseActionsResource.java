package uk.gov.digital.ho.hocs.casework.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.Authorised;

import java.util.UUID;

@RestController
public class CaseActionsResource {

    private final CaseActionService caseActionService;
    private final ActionDataDeadlineExtensionService extensionService;
    private final ActionDataAppealsService appealsService;
    private final ActionDataExternalInterestService externalInterestService;
    private final CaseDataService caseDataService;

    @Autowired
    public CaseActionsResource(CaseActionService caseActionService, ActionDataDeadlineExtensionService extensionService,
                               ActionDataAppealsService appealsService, ActionDataExternalInterestService externalInterestService,
                               CaseDataService caseDataService) {
        this.caseActionService = caseActionService;
        this.extensionService = extensionService;
        this.appealsService = appealsService;
        this.externalInterestService = externalInterestService;
        this.caseDataService = caseDataService;
    }

    @Authorised(accessLevel = AccessLevel.SUMMARY, permittedLowerLevels = {AccessLevel.RESTRICTED_OWNER})
    @GetMapping(path = "/case/{caseId}/actions")
    public ResponseEntity<CaseActionDataResponseDto> getAllCaseActionDataForCase(@PathVariable UUID caseId) {
        CaseActionDataResponseDto caseActionData = caseActionService.getAllCaseActionDataForCase(caseId);
        return ResponseEntity.ok(caseActionData);
    }

    // ---- Deadline Extensions ----
    @PostMapping(path = "/case/{caseUUID}/stage/{stageUUID}/actions/extension")
    public ResponseEntity<GetCaseReferenceResponse> createExtension(@PathVariable UUID caseUUID,
                                                                    @PathVariable UUID stageUUID,
                                                                    @RequestBody ActionDataDeadlineExtensionInboundDto extensionData) {

        extensionService.createExtension(caseUUID, stageUUID, extensionData);
        return ResponseEntity.ok(GetCaseReferenceResponse.from(caseUUID, caseDataService.getCaseRef(caseUUID)));
    }

    // ---- Case Appeals ----
    @PostMapping(path = "/case/{caseUUID}/stage/{stageUUID}/actions/appeal")
    public ResponseEntity<CreateActionDataResponse> createAppeal(@PathVariable UUID caseUUID,
                                                                 @PathVariable UUID stageUUID,
                                                                 @RequestBody ActionDataAppealDto appealData) {

        final UUID actionUUID = appealsService.createAppeal(caseUUID, stageUUID, appealData);
        return ResponseEntity.ok(new CreateActionDataResponse(actionUUID, caseUUID, caseDataService.getCaseRef(caseUUID)));
    }

    @PutMapping(path = "/case/{caseUUID}/stage/{stageUUID}/actions/appeal/{appealUUID}")
    public ResponseEntity<GetCaseReferenceResponse> updateAppeal(@PathVariable UUID caseUUID,
                                                                 @PathVariable UUID stageUUID,
                                                                 @PathVariable UUID appealUUID,
                                                                 @RequestBody ActionDataAppealDto extensionData) {

        appealsService.updateAppeal(caseUUID, appealUUID, extensionData);
        return ResponseEntity.ok(GetCaseReferenceResponse.from(caseUUID, caseDataService.getCaseRef(caseUUID)));
    }

    // ---- Register External Interest ----
    @PostMapping(path = "/case/{caseUUID}/stage/{stageUUID}/actions/interest")
    public ResponseEntity<GetCaseReferenceResponse> createExternalInterest(@PathVariable UUID caseUUID,
                                                                           @PathVariable UUID stageUUID,
                                                                           @RequestBody ActionDataExternalInterestInboundDto interestData) {

        externalInterestService.createExternalInterest(caseUUID, stageUUID, interestData);
        return ResponseEntity.ok(GetCaseReferenceResponse.from(caseUUID, caseDataService.getCaseRef(caseUUID)));
    }

    @PutMapping(path = "/case/{caseUUID}/stage/{stageUUID}/actions/interest/{interestUUID}")
    public ResponseEntity<GetCaseReferenceResponse> updateExternalInterest(@PathVariable UUID caseUUID,
                                                                    @PathVariable UUID stageUUID,
                                                                    @PathVariable UUID interestUUID,
                                                                    @RequestBody ActionDataExternalInterestInboundDto extensionData) {

        externalInterestService.updateExternalInterest(caseUUID, interestUUID, extensionData);
        return ResponseEntity.ok(GetCaseReferenceResponse.from(caseUUID, caseDataService.getCaseRef(caseUUID)));
    }
}
