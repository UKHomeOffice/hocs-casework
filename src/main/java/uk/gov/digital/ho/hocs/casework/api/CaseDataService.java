package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseSummaryResponse;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Service
@Slf4j
public class CaseDataService {

    private final CaseDataRepository caseDataRepository;
    private final AuditClient auditClient;
    private final ObjectMapper objectMapper;
    private final InfoClient infoClient;
    private final CorrespondentService correspondentService;
    private final StageService stageService;

    @Autowired
    public CaseDataService(CaseDataRepository caseDataRepository, InfoClient infoClient,
                           ObjectMapper objectMapper, CorrespondentService correspondentService,
                            StageService stageService, AuditClient auditClient) {
        this.caseDataRepository = caseDataRepository;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
        this.objectMapper = objectMapper;
        this.correspondentService = correspondentService;
        this.stageService = stageService;
    }

    public CaseData getCase(UUID caseUUID) {
        log.debug("Getting Case: {}", caseUUID);
        CaseData caseData = caseDataRepository.findByUuid(caseUUID);
        if (caseData != null) {
            log.info("Got Case: {}", caseUUID, value(EVENT, CASE_RETRIEVED));
            return caseData;
        } else {
            log.error("Case: {}, not found!", caseUUID, value(EVENT, CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case: %s, not found!", caseUUID), CASE_NOT_FOUND);
        }
    }

    public String getCaseType(UUID caseUUID) {
        log.debug("Getting CaseType for Case {}", caseUUID);
        CaseDataType caseDataType = infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34));
        if (caseDataType != null) {
            return caseDataType.getDisplayCode();
        } else {
            log.warn("Cannot determine type of caseUUID {} falling back to database lookup", caseUUID);
            return getCase(caseUUID).getType();
        }
    }

    CaseData createCase(CaseDataType caseType, Map<String, String> data, LocalDate caseDeadline, LocalDate dateReceived) {
        log.debug("Creating new Case of Type {}", caseType);
        Long caseNumber = caseDataRepository.getNextSeriesId();
        CaseData caseData = new CaseData(caseType, caseNumber, data, objectMapper, caseDeadline, dateReceived);
        caseDataRepository.save(caseData);
        auditClient.createCaseAudit(caseData);
        log.info("Created Case of Type: {} UUID: {}", caseType.getDisplayCode(), caseData.getUuid(), value(EVENT, CASE_CREATED));
        return caseData;
    }

    void updateCaseData(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        if (data != null) {
            CaseData caseData = getCase(caseUUID);
            caseData.update(data, objectMapper);
            caseDataRepository.save(caseData);
            log.info("Updated Case Data for Case: {} from stage {}", caseUUID, stageUUID, value(EVENT, CASE_UPDATED));
        }
    }

    void updatePriority(UUID caseUUID, boolean priority) {
        log.debug("Setting Case Priority {} for Case {}", priority, caseUUID);
        CaseData caseData = getCase(caseUUID);
        caseData.setPriority(priority);
        caseDataRepository.save(caseData);
        log.info("Setting Case Priority {} for Case {}", priority, caseUUID, value(EVENT, PRIORITY_UPDATED));
    }

    void deleteCase(UUID caseUUID) {
        log.debug("Deleting Case {}", caseUUID);
        CaseData caseData = getCase(caseUUID);
        caseData.setDeleted(true);
        caseDataRepository.save(caseData);
        log.info("Deleted Case: {}", caseUUID, value(EVENT, CASE_DELETED));
    }

    GetCaseSummaryResponse getCaseSummary(UUID caseUUID) {
        CaseData caseData = getCase(caseUUID);

        // All Stage Deadlines
        Map<String, LocalDate> stageDeadlines = infoClient.getDeadlines(caseData.getType(), caseData.getDateReceived());

        // Field Data
        Set<String> fieldSchema = infoClient.getCaseSummaryFields(caseData.getType());
        Map<String, String> additionalData = caseData.getFilteredDataMap(fieldSchema, objectMapper);

        // Primary Correspondent
        Correspondent correspondent = null;
        if (caseData.getPrimaryCorrespondentUUID() != null) {
            try {
                correspondent = correspondentService.getCorrespondent(caseData.getUuid(), caseData.getPrimaryCorrespondentUUID());
            } catch (ApplicationExceptions.EntityNotFoundException e) {
                log.warn("Case {} referenced a non-existent Correspondent {}", caseUUID, caseData.getPrimaryCorrespondentUUID(), value(EVENT, INVALID_PRIMARY_CORRESPONDENT));
            }
        }

        // Active Stages
        Set<Stage> activeStages = stageService.getActiveStagesByCaseUUID(caseUUID);

        return GetCaseSummaryResponse.from(caseData.getCaseDeadline(), stageDeadlines, additionalData, correspondent, activeStages);
    }
}
