package uk.gov.digital.ho.hocs.casework.migration.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.api.StageService;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateMigrationCaseRequest;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

@RestController
@Slf4j
public class MigrationCaseResource {

    private final MigrationCaseService migrationCaseService;
    private final StageService stageService;


    public MigrationCaseResource(MigrationCaseService migrationCaseService, StageService stageService) {
        this.migrationCaseService = migrationCaseService;
        this.stageService = stageService;
    }

    @PostMapping(value = "/migrate")
    public ResponseEntity<CreateCaseResponse> createMigrationCase(@RequestBody CreateMigrationCaseRequest request) {
        CaseData caseData = migrationCaseService.createMigrationCase(request.getType(), request.getStageType(), request.getData(), request.getDateReceived());
        return ResponseEntity.ok(CreateCaseResponse.from(caseData));
    }
}
