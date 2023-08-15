package uk.gov.digital.ho.hocs.casework.migration.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.BatchUpdateCaseDataRequest;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.BatchUpdateCaseDataResponse;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.CreateMigrationCaseRequest;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.CreateMigrationCaseResponse;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.CreateMigrationCorrespondentRequest;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.CreatePrimaryTopicRequest;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.UpdateCaseDataRequest;

import java.util.List;
import java.util.stream.Collectors;

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
            request.getCaseDeadline(),
            request.getDateCompleted(),
            request.getDateCreated(),
            request.getMigratedReference()
            );

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/migrate/correspondent")
    public ResponseEntity<Void> createMigrationCorrespondent(@RequestBody CreateMigrationCorrespondentRequest request) {
        migrationCaseService.createCorrespondents(
            request.getCaseId(),
            request.getStageId(),
            request.getPrimaryCorrespondent(),
            request.getAdditionalCorrespondents());

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/migrate/primary-topic")
    public ResponseEntity<Void> createMigrationPrimaryTopic(@RequestBody CreatePrimaryTopicRequest request) {
        migrationCaseService.createPrimaryTopic(request.getCaseId(), request.getStageId(), request.getTopicId());

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/migrate/case/{migratedReference}/case-data")
    public ResponseEntity<Void> updateCaseData(@PathVariable String migratedReference, @RequestBody UpdateCaseDataRequest request) {
        migrationCaseService.updateCaseData(migratedReference, request.getUpdateEventTimestamp(), request.getData());

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/migrate/case/case-data")
    public ResponseEntity<List<BatchUpdateCaseDataResponse>> batchUpdateCaseData(@RequestBody List<BatchUpdateCaseDataRequest> requests) {
        List<BatchUpdateCaseDataResponse> results = requests.stream().map(request -> {
            try {
                migrationCaseService.updateCaseData(request.getMigratedReference(), request.getUpdateEventTimestamp(), request.getData());
                return BatchUpdateCaseDataResponse.success(request.getMigratedReference());
            } catch (Exception e) {
                return BatchUpdateCaseDataResponse.error(request.getMigratedReference(), e.getMessage());
            }
        }).collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }
}
