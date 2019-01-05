package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            CaseDataType caseDataType = infoClient.getCaseType(shortCode);
            caseType = caseDataType.getType();
        } catch(ApplicationExceptions.ResourceException e) {
            log.warn("Cannot determine type of caseUUID {} falling back to database lookup", caseUUID, value(EVENT, CASE_TYPE_LOOKUP_FAILED) );
            caseType = getCase(caseUUID).getType();
        }
        log.debug("CaseType {} found for Case: {}", caseType, caseUUID);
        return caseType;
    }

    CaseData createCase(CaseDataType caseType, Map<String, String> data, LocalDate caseDeadline, LocalDate dateReceived) {
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
        log.debug("Updating priority for Case: {} Priority: {}", caseUUID, priority);
        CaseData caseData = getCase(caseUUID);
        caseData.setPriority(priority);
        caseDataRepository.save(caseData);
        log.info("Updated priority Case: {} Priority: {}", caseUUID, priority, value(EVENT, PRIORITY_UPDATED));
    }

    void updatePrimaryCorrespondent(UUID caseUUID, UUID stageUUID, UUID primaryCorrespondentUUID) {
        log.debug("Updating Primary Correspondent for Case: {} Correspondent: {}", caseUUID, primaryCorrespondentUUID);
        CaseData caseData = getCase(caseUUID);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);
        caseDataRepository.save(caseData);
        log.info("Updated Primary Correspondent for Case: {} Correspondent: {}", caseUUID, primaryCorrespondentUUID, value(EVENT, PRIMARY_CORRESPONDENT_UPDATED));
    }

    void updatePrimaryTopic(UUID caseUUID, UUID stageUUID, UUID primaryTopicUUID) {
        log.debug("Updating Primary Topic for Case: {} Topic: {}", caseUUID, primaryTopicUUID);
        CaseData caseData = getCase(caseUUID);
        caseData.setPrimaryTopicUUID(primaryTopicUUID);
        caseDataRepository.save(caseData);
        log.info("Updated Primary Topic for Case: {} Correspondent: {}", caseUUID, primaryTopicUUID, value(EVENT, PRIMARY_TOPIC_UPDATED));
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
        HocsFormData[] dataFilter = infoClient.getCaseSummaryFields(caseData.getType());
        Stream<HocsFormField> fields = Arrays.stream(dataFilter).map(f -> f.getData());
        Set<HocsFormProperty> properties = fields.map(field -> field.props).collect(Collectors.toSet());
        Map<String, String> filteredData = caseData.getFilteredDataMap(properties, objectMapper);
        log.debug("filteredData size: {}", filteredData.size());


        // All Stage Deadlines
        Map<String, String> stageDeadlines = infoClient.getDeadlines(caseData.getType(), caseData.getDateReceived());

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
