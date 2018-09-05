package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.*;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;

import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/case/{caseUUID}")
class StageDataResource {

    private final StageDataService stageDataService;

    @Autowired
    public StageDataResource(StageDataService stageDataService) {
        this.stageDataService = stageDataService;
    }

    @PostMapping(value = "/stage")
    public ResponseEntity<CreateStageResponse> createStage(@PathVariable UUID caseUUID, @RequestBody CreateStageRequest request) {
        StageData stageData = stageDataService.createStage(caseUUID, request.getType(), request.getTeamUUID(), request.getUserUUID());
        return ResponseEntity.ok(CreateStageResponse.from(stageData));
    }

    @GetMapping(value = "/stage")
    public ResponseEntity<GetStagesResponse> getStage(@PathVariable UUID caseUUID) {
        Set<StageData> stageDatas = stageDataService.getStagesForCase(caseUUID);
        return ResponseEntity.ok(GetStagesResponse.from(stageDatas));
    }

    @PostMapping(value = "/stage/{stageUUID}/allocate")
    public ResponseEntity allocateStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody AllocateStageRequest allocateStageRequest) {
        stageDataService.allocateStage(stageUUID, allocateStageRequest.getTeamUUID(), allocateStageRequest.getUserUUID());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/stage/{stageUUID}/close")
    public ResponseEntity closeStage(@PathVariable UUID stageUUID) {
        stageDataService.closeStage(stageUUID);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/stage/{stageUUID}/reopen")
    public ResponseEntity reopenStage(@PathVariable UUID stageUUID) {
        stageDataService.reopenStage(stageUUID);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/stage/{stageUUID}")
    public ResponseEntity<GetStageResponse> getStage(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID) {
        StageData stageData = stageDataService.getStage(stageUUID);
        return ResponseEntity.ok(GetStageResponse.from(stageData));
    }

}