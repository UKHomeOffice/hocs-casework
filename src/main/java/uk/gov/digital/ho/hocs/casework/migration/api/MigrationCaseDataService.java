package uk.gov.digital.ho.hocs.casework.migration.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentClient;
import uk.gov.digital.ho.hocs.casework.client.documentclient.dto.CreateCaseworkDocumentRequest;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.CaseAttachment;
import uk.gov.digital.ho.hocs.casework.migration.client.auditclient.MigrationAuditClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.MIGRATION_CASE_NOT_FOUND;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.MIGRATION_CASE_NOT_UPDATED_NULL_DATA;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.MIGRATION_CASE_RETRIEVED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.MIGRATION_CASE_UPDATED;

@Service
@Slf4j
public class MigrationCaseDataService {

    protected final CaseDataRepository caseDataRepository;

    protected final MigrationAuditClient migrationAuditClient;

    protected final DocumentClient documentClient;

    protected final InfoClient infoClient;

    public MigrationCaseDataService(CaseDataRepository caseDataRepository,
                                    DocumentClient documentClient,
                                    InfoClient infoClient,
                                    MigrationAuditClient migrationAuditClient) {
        this.caseDataRepository = caseDataRepository;
        this.documentClient = documentClient;
        this.infoClient = infoClient;
        this.migrationAuditClient = migrationAuditClient;
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
        migrationAuditClient.updateCaseAudit(caseData, stageUUID);
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
        migrationAuditClient.createCaseAudit(caseData);
        migrationAuditClient.completeCaseAudit(caseData);
        return caseData;
    }

    void createCaseAttachments(UUID caseId, List<CaseAttachment> caseAttachemnts) {
        for(CaseAttachment attachment : caseAttachemnts) {
            CreateCaseworkDocumentRequest document = new CreateCaseworkDocumentRequest(attachment.getDisplayName(), attachment.getType(), attachment.getDocumentPath(), caseId);
            documentClient.createDocument(caseId, document);
        }
    }

}
