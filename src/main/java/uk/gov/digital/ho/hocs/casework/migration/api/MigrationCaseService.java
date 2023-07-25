package uk.gov.digital.ho.hocs.casework.migration.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.TopicService;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.CreateMigrationCaseResponse;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.MigrationComplaintCorrespondent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class MigrationCaseService {

    protected final MigrationCaseDataService migrationCaseDataService;

    protected final MigrationStageService migrationStageService;

    private final TopicService topicService;

    private final CaseDataService caseDataService;

    public MigrationCaseService(
        MigrationCaseDataService migrationCaseDataService,
        MigrationStageService migrationStageService,
        TopicService topicService,
        CaseDataService caseDataService
    ) {
        this.migrationCaseDataService = migrationCaseDataService;
        this.migrationStageService = migrationStageService;
        this.topicService = topicService;
        this.caseDataService = caseDataService;
    }

    CreateMigrationCaseResponse createMigrationCase(
        String caseType,
        String stageType,
        Map<String, String> data,
        LocalDate dateReceived,
        LocalDate caseDeadline,
        LocalDate dateCompleted,
        LocalDate dateCreated,
        String migratedCaseReference)
    {
        log.debug("Migrating Case of type: {}", caseType);

        CaseData caseData = migrationCaseDataService.createCase(caseType, data, dateReceived, caseDeadline,
            dateCompleted, dateCreated, migratedCaseReference);
        Stage stage = null;
        if(dateCompleted != null) {
            stage = migrationStageService.createStageForClosedCase(caseData.getUuid(), stageType);
        }

        log.debug(
            "Migrated Case ID: {} at Stage ID: {} with Case Reference: {}", caseData.getUuid(), stage != null ? stage.getUuid() : null,
            caseData.getReference()
                 );

        return CreateMigrationCaseResponse.from(caseData, stage != null ? stage.getUuid() : null);
    }

    public void createCorrespondents(
        UUID caseId,
        UUID stageId,
        MigrationComplaintCorrespondent primaryCorrespondent,
        List<MigrationComplaintCorrespondent> additionalCorrespondents
                                    ) {
        migrationCaseDataService.createPrimaryCorrespondent(primaryCorrespondent, caseId, stageId);
        migrationCaseDataService.createAdditionalCorrespondent(additionalCorrespondents, caseId, stageId);
    }

    public void createPrimaryTopic(UUID caseId, UUID stageId, UUID topicId) {
        Topic topic = topicService.createTopic(caseId, topicId);
        caseDataService.updatePrimaryTopic(caseId, stageId, topic.getUuid());
    }

    public void updateCaseData(
        String migratedReference, LocalDateTime updateEventTimestamp, Map<String, String> data
                              ) {
        migrationCaseDataService.updateCaseData(migratedReference, updateEventTimestamp, data);
    }

}
