package uk.gov.digital.ho.hocs.casework.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseReferenceResponse;


import java.util.UUID;

@RestController
public class CaseActionsResource {

    private final CaseActionService caseActionService;
    private final CaseDataService caseDataService;

    @Autowired
    public CaseActionsResource(CaseActionService caseActionService, CaseDataService caseDataService) {
        this.caseActionService = caseActionService;
        this.caseDataService = caseDataService;
    }

    @PostMapping(path = "/case/{caseUUID}/stage/{stageUUID}/caseType/{caseType}/action")
    public ResponseEntity<GetCaseReferenceResponse> createActionData(@PathVariable UUID caseUUID,
                                                                     @PathVariable UUID stageUUID,
                                                                     @PathVariable String caseType,
                                                                     @RequestBody ActionDataDto actionData) {

        caseActionService.createActionDataForCase(caseUUID, stageUUID, caseType, actionData);
        return ResponseEntity.ok(GetCaseReferenceResponse.from(caseUUID, caseDataService.getCaseRef(caseUUID)));
    }

    @PutMapping(path = "/case/{caseUUID}/stage/{stageUUID}/caseType/{caseType}/action")
    public ResponseEntity<GetCaseReferenceResponse> updateActionData(@PathVariable UUID caseUUID,
                                                                     @PathVariable UUID stageUUID,
                                                                     @PathVariable String caseType,
                                                                     @RequestBody ActionDataDto actionData){

        caseActionService.updateActionDataForCase(caseUUID, stageUUID, caseType, actionData);
        return ResponseEntity.ok(GetCaseReferenceResponse.from(caseUUID, caseDataService.getCaseRef(caseUUID)));
    }
}
