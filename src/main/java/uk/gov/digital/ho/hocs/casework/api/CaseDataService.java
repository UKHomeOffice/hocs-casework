package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.GetStandardLineResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetTemplateResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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

    @Autowired
    public CaseDataService(CaseDataRepository caseDataRepository, InfoClient infoClient,
                           ObjectMapper objectMapper, AuditClient auditClient) {
        this.caseDataRepository = caseDataRepository;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
        this.objectMapper = objectMapper;
    }

    public CaseData getCase(UUID caseUUID) {
        CaseData caseData = getCaseData(caseUUID);
        auditClient.viewCaseAudit(caseData);
        return caseData;
    }


    private CaseData getCaseData(UUID caseUUID) {
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
            caseType = caseDataType.getDisplayCode();
        } catch(ApplicationExceptions.ResourceException e) {
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
        auditClient.createCaseAudit(caseData.getUuid(), null, caseData.getReference());
        log.info("Created Case: {} Ref: {} UUID: {}", caseData.getUuid(), caseData.getReference(), caseData.getUuid(), value(EVENT, CASE_CREATED));
        return caseData;
    }

    void updateCaseData(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        log.debug("Updating data for Case: {}", caseUUID);
        if (data != null) {
            log.debug("Data size {}", data.size());
            CaseData caseData = getCaseData(caseUUID);
            caseData.update(data, objectMapper);
            caseDataRepository.save(caseData);
            auditClient.updateCaseAudit(caseData);
            log.info("Updated Case Data for Case: {} Stage: {}", caseUUID, stageUUID, value(EVENT, CASE_UPDATED));
        } else {
            log.warn("Data was null for Case: {} Stage: {}", caseUUID, stageUUID, value(EVENT, CASE_NOT_UPDATED_NULL_DATA));
        }
    }

    void updatePrimaryCorrespondent(UUID caseUUID, UUID stageUUID, UUID primaryCorrespondentUUID) {
        log.debug("Updating Primary Correspondent for Case: {} Correspondent: {}", caseUUID, primaryCorrespondentUUID);
        CaseData caseData = getCaseData(caseUUID);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData);
        log.info("Updated Primary Correspondent for Case: {} Correspondent: {}", caseUUID, primaryCorrespondentUUID, value(EVENT, PRIMARY_CORRESPONDENT_UPDATED));
    }

    void updatePrimaryTopic(UUID caseUUID, UUID stageUUID, UUID primaryTopicUUID) {
        log.debug("Updating Primary Topic for Case: {} Topic: {}", caseUUID, primaryTopicUUID);
        CaseData caseData = getCaseData(caseUUID);
        caseData.setPrimaryTopicUUID(primaryTopicUUID);
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData);
        log.info("Updated Primary Topic for Case: {} Correspondent: {}", caseUUID, primaryTopicUUID, value(EVENT, PRIMARY_TOPIC_UPDATED));
    }

    void updatePriority(UUID caseUUID, boolean priority) {
        log.debug("Updating priority for Case: {} Priority {}", caseUUID, priority);
        CaseData caseData = getCaseData(caseUUID);
        caseData.setPriority(priority);
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData);
        log.info("Updated priority Case: {} Priority {}", caseUUID, priority, value(EVENT, PRIORITY_UPDATED));
    }

    void deleteCase(UUID caseUUID) {
        log.debug("Deleting Case: {}", caseUUID);
        CaseData caseData = getCaseData(caseUUID);
        caseData.setDeleted(true);
        caseDataRepository.save(caseData);
        auditClient.deleteCaseAudit(caseData);
        log.info("Deleted Case: {}", caseUUID, value(EVENT, CASE_DELETED));
    }

    CaseSummary getCaseSummary(UUID caseUUID) {
        log.debug("Building CaseSummary for Case: {}", caseUUID);

        CaseData caseData = getCaseData(caseUUID);
        FieldDto[] summaryFields = infoClient.getCaseSummaryFields(caseData.getType());
        Map<String, String> caseDataMap = caseData.getDataMap(objectMapper);
        Set<AdditionalField> additionalFields = Arrays.stream(summaryFields)
                .map(field -> new AdditionalField(field.getLabel(), caseDataMap.getOrDefault(field.getName(), ""), field.getComponent()))
                .collect(Collectors.toSet());
        Map<String, String> stageDeadlines = infoClient.getDeadlines(caseData.getType(), caseData.getDateReceived());

        log.info("Got Case Summary for Case: {} Ref: {}", caseData.getUuid(), caseData.getReference(), value(EVENT, CASE_SUMMARY_RETRIEVED));

        CaseSummary caseSummary = new CaseSummary(
                caseData.getCaseDeadline(),
                stageDeadlines,
                additionalFields,
                caseData.getPrimaryCorrespondent(),
                caseData.getPrimaryTopic(),
                caseData.getActiveStages());
        auditClient.viewCaseSummaryAudit(caseData, caseSummary);
        return caseSummary;
    }

    GetStandardLineResponse getStandardLine(UUID caseUUID) {
        CaseData caseData = getCaseData(caseUUID);
        auditClient.viewStandardLineAudit(caseData);
        return infoClient.getStandardLine(caseData.getPrimaryTopic().getTextUUID());
    }

    GetTemplateResponse getTemplate(UUID caseUUID) {
        CaseData caseData = getCaseData(caseUUID);
        auditClient.viewTemplateAudit(caseData);
        return infoClient.getTemplate(caseData.getType());
    }

    Stream<TimelineItem> getCaseTimeline(UUID caseUUID) {
        log.debug("Building Timeline for Case: {}", caseUUID);

        CaseData caseData = getCase(caseUUID);

        Set<GetAuditResponse> audit = auditClient.getAuditLinesForCase(caseUUID);
        Set<CaseNote> notes = caseData.getCaseNotes();

        Stream<TimelineItem> auditTimeline = audit.stream().map(a -> new TimelineItem(a.getCaseUUID(), a.getStageUUID(), a.getAuditTimestamp(), a.getUserID(), a.getType(), a.getAuditPayload()));
        Stream<TimelineItem> notesTimeline = notes.stream().map(n -> new TimelineItem(n.getCaseUUID(), null, n.getCreated(), null, n.getCaseNoteType(), n.getText()));

        return Stream.concat(auditTimeline, notesTimeline);

    }
}
