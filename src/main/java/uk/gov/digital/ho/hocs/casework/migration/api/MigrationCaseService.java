package uk.gov.digital.ho.hocs.casework.migration.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateStageRequest;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.time.LocalDate;
import java.util.Map;

@Service
@Slf4j
public class MigrationCaseService {

    protected final CaseDataRepository caseDataRepository;
    protected final MigrationStageService migrationStageService;
    protected final InfoClient infoClient;

    public MigrationCaseService(CaseDataRepository caseDataRepository, InfoClient infoClient, MigrationStageService migrationStageService) {
        this.caseDataRepository = caseDataRepository;
        this.infoClient = infoClient;
        this.migrationStageService = migrationStageService;
    }

    CaseData createMigrationCase(String caseType, String stageType, Map<String, String> data, LocalDate dateReceived) {
        log.debug("Migrating Case of type: {}", caseType);
        Long caseNumber = caseDataRepository.getNextSeriesId();
        CaseDataType caseDataType = infoClient.getCaseType(caseType);
        CaseData caseData = new CaseData(caseDataType, caseNumber, data, dateReceived);
        LocalDate deadline = LocalDate.now();
        caseData.setCaseDeadline(deadline);
        caseDataRepository.save(caseData);
        // create stage for case
        CreateStageRequest createStageRequest = new CreateStageRequest(stageType, null, null, "", null, null);
        Stage stage = migrationStageService.createStageForClosedCase(caseData.getUuid(), createStageRequest);
        return caseData;
    }
}
