package uk.gov.digital.ho.hocs.casework.migration.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.MIGRATION_CASE_NOT_FOUND;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.MIGRATION_CASE_NOT_UPDATED_NULL_DATA;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.MIGRATION_CASE_RETRIEVED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.MIGRATION_CASE_UPDATED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.PRIMARY_CORRESPONDENT_UPDATED;

@Service
@Slf4j
public class MigrationCaseDataService {

    protected final CaseDataRepository caseDataRepository;

    protected final AuditClient auditClient;

    protected final InfoClient infoClient;

    public MigrationCaseDataService(CaseDataRepository caseDataRepository,
                                    InfoClient infoClient,
                                    AuditClient auditClient) {
        this.caseDataRepository = caseDataRepository;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
    }

    protected CaseData getCaseData(UUID caseUUID) {
        log.debug("Getting Case: {}", caseUUID);
        CaseData caseData = caseDataRepository.findActiveByUuid(caseUUID);
        if (caseData==null) {
            log.error("Migration Case: {}, not found!", caseUUID, value(EVENT, MIGRATION_CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case: %s, not found!", caseUUID),
                MIGRATION_CASE_NOT_FOUND);
        }
        log.info("Got Migration Case: {}", caseData.getUuid(), value(EVENT, MIGRATION_CASE_RETRIEVED));
        return caseData;
    }

    public void updateCaseData(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        if (data==null) {
            log.warn("Data was null for Case: {} Stage: {}", caseUUID, stageUUID,
                value(EVENT, MIGRATION_CASE_NOT_UPDATED_NULL_DATA));
            return;
        }
        updateCaseData(getCaseData(caseUUID), stageUUID, data);
    }

    public void updateCaseData(CaseData caseData, UUID stageUUID, Map<String, String> data) {
        log.debug("Updating data for Case: {}", caseData.getUuid());
        if (data==null) {
            log.warn("Data was null for Case: {} Stage: {}", caseData.getUuid(), stageUUID,
                value(EVENT, MIGRATION_CASE_NOT_UPDATED_NULL_DATA));
            return;
        }
        log.debug("Data size {}", data.size());
        caseData.update(data);
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
        log.info("Updated Case Data for Case: {} Stage: {}", caseData.getUuid(), stageUUID,
            value(EVENT, MIGRATION_CASE_UPDATED));
    }

    CaseData createCompletedCase(String caseType, Map<String, String> data, LocalDate dateReceived) {
        log.debug("Creating Case of type: {}", caseType);
        Long caseNumber = caseDataRepository.getNextSeriesId();
        CaseDataType caseDataType = infoClient.getCaseType(caseType);
        CaseData caseData = new CaseData(caseDataType, caseNumber, data, dateReceived);
        LocalDate deadline = LocalDate.now();
        caseData.setCaseDeadline(deadline);
        caseData.setCompleted(true);
        caseDataRepository.save(caseData);
        auditClient.createCaseAudit(caseData);
        return caseData;
    }

    public void updatePrimaryCorrespondent(UUID caseUUID, UUID stageUUID, UUID primaryCorrespondentUUID) {
        log.debug("Updating Primary Correspondent for Migrated Case: {} Correspondent: {}", caseUUID, primaryCorrespondentUUID);
        CaseData caseData = getCaseData(caseUUID);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
        log.info("Updated Primary Correspondent for Migrated Case: {} Correspondent: {}", caseUUID, primaryCorrespondentUUID,
            value(EVENT, PRIMARY_CORRESPONDENT_UPDATED));
    }
}
