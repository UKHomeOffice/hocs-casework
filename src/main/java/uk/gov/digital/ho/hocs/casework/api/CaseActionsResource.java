package uk.gov.digital.ho.hocs.casework.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseActionDataResponseDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseReferenceResponse;


import java.util.List;
import java.util.Map;
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
    public ResponseEntity<?> createActionData(@PathVariable UUID caseUUID,
                                                                     @PathVariable UUID stageUUID,
                                                                     @PathVariable String caseType,
                                                                     @RequestBody ActionDataDto actionData) {

        caseActionService.createActionDataForCase(caseUUID, stageUUID, caseType, actionData);
        return ResponseEntity.ok(GetCaseReferenceResponse.from(caseUUID, caseDataService.getCaseRef(caseUUID)));
    }

    @PutMapping(path = "/case/{caseUUID}/stage/{stageUUID}/caseType/{caseType}/action/{actionId}")
    public ResponseEntity<GetCaseReferenceResponse> updateActionData(@PathVariable UUID caseUUID,
                                                                     @PathVariable UUID stageUUID,
                                                                     @PathVariable String caseType,
                                                                     @PathVariable UUID actionId,
                                                                     @RequestBody ActionDataDto actionData){

        caseActionService.updateActionDataForCase(caseUUID, stageUUID, caseType, actionId, actionData);
        return ResponseEntity.ok(GetCaseReferenceResponse.from(caseUUID, caseDataService.getCaseRef(caseUUID)));
    }

    @GetMapping(path = "/case/{caseId}/actions")
    public ResponseEntity<CaseActionDataResponseDto> getAllCaseActionDataForCase(@PathVariable UUID caseId) {
        CaseActionDataResponseDto caseActionData = caseActionService.getAllCaseActionDataForCase(caseId);
        return ResponseEntity.ok(caseActionData);
    }
}
