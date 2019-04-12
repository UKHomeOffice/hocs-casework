package uk.gov.digital.ho.hocs.casework.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdateCaseDataRequest;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.Allocated;
import uk.gov.digital.ho.hocs.casework.security.AllocationLevel;
import uk.gov.digital.ho.hocs.casework.security.Authorised;

import java.util.UUID;


@Slf4j
@RestController
public class MigrationCaseDataResource {

    private final MigrationCaseDataService migrationCaseDataService;

    @Autowired
    public MigrationCaseDataResource(MigrationCaseDataService migrationCaseDataService) {
        this.migrationCaseDataService = migrationCaseDataService;
    }

    @Authorised(accessLevel = AccessLevel.WRITE)
    @PostMapping(value = "/migration/case")
    ResponseEntity<CreateCaseResponse> createCase(@RequestBody MigrationCreateCaseRequest request) {
        CaseData caseData = migrationCaseDataService.createCase(request.getType(), request.getCaseReference(), request.getData(), request.getCaseDeadline(), request.getDateReceived());
        return ResponseEntity.ok(CreateCaseResponse.from(caseData));
    }

    @GetMapping(value = "/migration/case/{caseUUID}")
    ResponseEntity<UUID> getStageUUID(@PathVariable UUID caseUUID){
        UUID stageUUID = migrationCaseDataService.getStageUUID(caseUUID);
        return ResponseEntity.ok(stageUUID);
    }

    @Allocated(allocatedTo = AllocationLevel.USER)
    @PutMapping(value = "/migration/case/{caseUUID}/stage/{stageUUID}/data")
    ResponseEntity updateCaseData(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @RequestBody UpdateCaseDataRequest request) {
        migrationCaseDataService.updateCaseData(caseUUID, stageUUID, request.getData());
        return ResponseEntity.ok().build();
    }
}
