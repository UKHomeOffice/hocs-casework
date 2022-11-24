package uk.gov.digital.ho.hocs.casework.migration.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.CorrespondentService;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.MigrationComplaintCorrespondent;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

    CaseData createMigrationCase(String caseType, String stageType, Map<String, String> data, LocalDate dateReceived,
                                 MigrationComplaintCorrespondent primaryCorrespondent, List<MigrationComplaintCorrespondent> additionalCorrespondents) {
        log.debug("Migrating Case of type: {}", caseType);

        CaseData caseData = migrationCaseDataService.createCompletedCase(caseType, data, dateReceived);
        Stage stage = migrationStageService.createStageForClosedCase(caseData.getUuid(), stageType);
        migrationCaseDataService.createPrimaryCorrespondent(primaryCorrespondent, caseData.getUuid(), stage.getUuid());
        migrationCaseDataService.createAdditionalCorrespondent(additionalCorrespondents, caseData.getUuid(), stage.getUuid());
        return caseData;
    }
}