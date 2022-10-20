package uk.gov.digital.ho.hocs.casework.migration.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MigrationCaseService {

    protected final MigrationCaseDataService migrationCaseDataService;

    protected final MigrationStageService migrationStageService;

    public MigrationCaseService(MigrationCaseDataService migrationCaseDataService,
                                MigrationStageService migrationStageService) {
        this.migrationCaseDataService = migrationCaseDataService;
        this.migrationStageService = migrationStageService;
    }

    CaseData createMigrationCase(String caseType, String stageType, Map<String, String> data, LocalDate dateReceived, List caseAttachments) {
        log.debug("Migrating Case of type: {}", caseType);
        CaseData caseData = migrationCaseDataService.createCompletedCase(caseType, data, dateReceived);
        migrationCaseDataService.createCaseAttachments(caseData.getUuid(), caseAttachments);
        Stage stage = migrationStageService.createStageForClosedCase(caseData.getUuid(), stageType);
        return caseData;
    }

}
