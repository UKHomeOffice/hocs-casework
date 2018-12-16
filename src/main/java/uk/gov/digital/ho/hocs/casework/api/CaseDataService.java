package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.auditClient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseSummary;
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
            log.info("Got Case: {}", caseData.getUuid(), value(EVENT, CASE_RETRIEVED));
            return caseData;
        } else {
            log.error("Case: {}, not found!", caseUUID, value(EVENT, CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case: %s, not found!", caseUUID), CASE_NOT_FOUND);
        }
    }

    public String getCaseType(UUID caseUUID) {
        String shortCode = caseUUID.toString().substring(34);
        log.debug("Looking up CaseType for Case: {} Shortcode: {}", caseUUID, shortCode);
        String caseType;
        try {
            CaseDataType caseDataType = infoClient.getCaseTypeByShortCode(shortCode);
            caseType = caseDataType.getDisplayCode();
        } catch(ApplicationExceptions.ClientException e) {
            log.warn("Cannot determine type of caseUUID {} falling back to database lookup", caseUUID, value(EVENT, CASE_TYPE_LOOKUP_FAILED) );
            caseType = getCase(caseUUID).getType();
        }
        log.debug("CaseType {} found for Case: {}", caseType, caseUUID);
        return caseType;
    }

    CaseData createCase(CaseDataType caseType, Map<String, String> data, LocalDate caseDeadline, LocalDate dateReceived) {
        log.debug("Creating Case of type: {}", caseType);
        Long caseNumber = caseDataRepository.getNextSeriesId();
        log.debug("Allocating Ref: {}", caseNumber);
        CaseData caseData = new CaseData(caseType, caseNumber, data, objectMapper, caseDeadline, dateReceived);
        caseDataRepository.save(caseData);
        auditClient.createCaseAudit(caseData);
        log.info("Created Case: {} Ref: {} UUID: {}", caseData.getUuid(), caseData.getReference(), caseData.getUuid(), value(EVENT, CASE_CREATED));
        return caseData;
    }

    void updateCaseData(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        log.debug("Updating data for Case: {}", caseUUID);
        if (data != null) {
            log.debug("Data size {}", data.size());
            CaseData caseData = getCase(caseUUID);
            caseData.update(data, objectMapper);
            caseDataRepository.save(caseData);
            log.info("Updated Case Data for Case: {} Stage: {}", caseUUID, stageUUID, value(EVENT, CASE_UPDATED));
        } else {
            log.warn("Data was null for Case: {} Stage: {}", caseUUID, stageUUID, value(EVENT, CASE_NOT_UPDATED_NULL_DATA));
        }
    }

    void updatePriority(UUID caseUUID, boolean priority) {
        log.debug("Updating priority for Case: {} Priority {}", caseUUID, priority);
        CaseData caseData = getCase(caseUUID);
        caseData.setPriority(priority);
        caseDataRepository.save(caseData);
        log.info("Updated priority Case: {} Priority {}", caseUUID, priority, value(EVENT, PRIORITY_UPDATED));
    }

    void deleteCase(UUID caseUUID) {
        log.debug("Deleting Case: {}", caseUUID);
        CaseData caseData = getCase(caseUUID);
        caseData.setDeleted(true);
        caseDataRepository.save(caseData);
        log.info("Deleted Case: {}", caseUUID, value(EVENT, CASE_DELETED));
    }

    CaseSummary getCaseSummary(UUID caseUUID) {
        log.debug("Building CaseSummary for Case: {}", caseUUID);

        CaseData caseData = getCase(caseUUID);

        // Field Data
        Set<String> dataFilter = infoClient.getCaseSummaryFields(caseData.getType());
        Map<String, String> filteredData = caseData.getFilteredDataMap(dataFilter, objectMapper);
        log.debug("filteredData size: {}", filteredData.size());


        // All Stage Deadlines
        Map<String, LocalDate> stageDeadlines = infoClient.getDeadlines(caseData.getType(), caseData.getDateReceived());

        // Primary Correspondent
        Correspondent correspondent = null;
        if (caseData.getPrimaryCorrespondentUUID() != null) {
            try {
                correspondent = correspondentService.getCorrespondent(caseData.getUuid(), caseData.getPrimaryCorrespondentUUID());
            } catch (ApplicationExceptions.EntityNotFoundException e) {
                // Do Nothing - correspondent is null.
            }
        } else {
            log.debug("PrimaryCorrespondentUUID for Case: {} was null", caseUUID);
        }

        // Active Stages
        Set<Stage> stages = stageService.getActiveStagesByCaseUUID(caseUUID);

        log.info("Got Case Summary for Case: {} Ref: {}", caseData.getUuid(), caseData.getReference(), value(EVENT, CASE_SUMMARY_RETRIEVED));
        return new CaseSummary(caseData.getCaseDeadline(), stageDeadlines, filteredData, correspondent, stages);
    }
}
