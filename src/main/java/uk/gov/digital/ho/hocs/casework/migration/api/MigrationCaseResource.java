package uk.gov.digital.ho.hocs.casework.migration.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.CreateMigrationCaseRequest;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.CreateMigrationCaseResponse;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.CreateMigrationCorrespondentRequest;

@RestController
@Slf4j
public class MigrationCaseResource {

    private final MigrationCaseService migrationCaseService;

    public MigrationCaseResource(MigrationCaseService migrationCaseService) {
        this.migrationCaseService = migrationCaseService;
    }

    @PostMapping(value = "/migrate/case")
    public ResponseEntity<CreateMigrationCaseResponse> createMigrationCase(@RequestBody CreateMigrationCaseRequest request) {
        CreateMigrationCaseResponse response = migrationCaseService.createMigrationCase(
            request.getType(),
            request.getStageType(),
            request.getData(),
            request.getDateReceived(),
            request.getDateCompleted(),
            request.getDateCreated(),
            request.getMigratedReference()
            );

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/migrate/correspondent")
    public ResponseEntity createMigrationCorrespondent(@RequestBody CreateMigrationCorrespondentRequest request) {
        migrationCaseService.createCorrespondents(
            request.getCaseId(),
            request.getStageId(),
            request.getPrimaryCorrespondent(),
            request.getAdditionalCorrespondents());

        return ResponseEntity.ok().build();
    }
}
