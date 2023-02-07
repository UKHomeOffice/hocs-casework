package uk.gov.digital.ho.hocs.casework.migration.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.CorrespondentService;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.CaseAttachment;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.CreateMigrationCaseResponse;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.MigrationComplaintCorrespondent;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class MigrationCaseService {

    protected final MigrationCaseDataService migrationCaseDataService;

    protected final MigrationStageService migrationStageService;

    private final CorrespondentService correspondentService;

    public MigrationCaseService(MigrationCaseDataService migrationCaseDataService,
                                MigrationStageService migrationStageService,
                                CorrespondentService correspondentService) {
        this.migrationCaseDataService = migrationCaseDataService;
        this.migrationStageService = migrationStageService;
        this.correspondentService = correspondentService;
    }

    CreateMigrationCaseResponse createMigrationCase(String caseType,
                                                    String stageType,
                                                    Map<String, String> data,
                                                    LocalDate dateReceived)
 {
        log.debug("Migrating Case of type: {}", caseType);

        CaseData caseData = migrationCaseDataService.createCompletedCase(caseType, data, dateReceived);
        Stage stage = migrationStageService.createStageForClosedCase(caseData.getUuid(), stageType);

        log.debug("Migrated Case ID: {} at Stage ID: {} with Case Reference: {}", caseData.getUuid(), stage.getUuid(), caseData.getReference());

        return CreateMigrationCaseResponse.from(caseData, stage.getUuid());
    }

    public void createCorrespondents(UUID caseId,
                                      UUID stageId,
                                      MigrationComplaintCorrespondent primaryCorrespondent,
                                      List<MigrationComplaintCorrespondent> additionalCorrespondents) {

        migrationCaseDataService.createPrimaryCorrespondent(primaryCorrespondent, caseId, stageId);
        migrationCaseDataService.createAdditionalCorrespondent(additionalCorrespondents, caseId, stageId);
    }

    public void createCaseAttachments(UUID caseId, List<CaseAttachment> caseAttachments) {
        migrationCaseDataService.createCaseAttachments(caseId, caseAttachments);
    }

}