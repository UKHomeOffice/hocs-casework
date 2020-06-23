package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetStandardLineResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.TemplateDto;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.AuditPayload;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.EntityDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.EntityTotalDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_COMPLETED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_DELETED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_TOPIC_DELETED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_UPDATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CORRESPONDENT_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CORRESPONDENT_DELETED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_COMPLETED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_RECREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.*;

@Service
@Slf4j
@Qualifier("CaseDataService")
public class CaseDataService {

    protected final CaseDataRepository caseDataRepository;
    protected final AuditClient auditClient;
    protected final ObjectMapper objectMapper;
    protected final InfoClient infoClient;

    @Autowired
    public CaseDataService(CaseDataRepository caseDataRepository, InfoClient infoClient,
                           ObjectMapper objectMapper, AuditClient auditClient) {
        this.caseDataRepository = caseDataRepository;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
        this.objectMapper = objectMapper;
    }

    public static final List<String> TIMELINE_EVENTS = List.of(
            CASE_CREATED.toString(),
            CASE_COMPLETED.toString(),
            CASE_TOPIC_CREATED.toString(),
            CASE_TOPIC_DELETED.toString(),
            STAGE_ALLOCATED_TO_TEAM.toString(),
            STAGE_CREATED.toString(),
            STAGE_RECREATED.toString(),
            STAGE_COMPLETED.toString(),
            STAGE_ALLOCATED_TO_USER.toString(),
            CORRESPONDENT_DELETED.toString(),
            CORRESPONDENT_CREATED.toString(),
            DOCUMENT_CREATED.toString(),
            DOCUMENT_DELETED.toString()
    );

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

    private CaseData getAnyCaseData(UUID caseUUID) {
        log.debug("Getting any Case: {}", caseUUID);
        CaseData caseData = caseDataRepository.findAnyByUuid(caseUUID);
        if (caseData != null) {
            log.info("Got any Case: {}", caseData.getUuid(), value(EVENT, CASE_RETRIEVED));
            return caseData;
        } else {
            log.error("Any Case: {}, not found!", caseUUID, value(EVENT, CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Any Case: %s, not found!", caseUUID), CASE_NOT_FOUND);
        }
    }

    public String getCaseRef(UUID caseUUID) {
        log.debug("Looking up CaseRef for Case: {}", caseUUID);
        String caseRef = caseDataRepository.getCaseRef(caseUUID);
        log.debug("CaseRef {} found for Case: {}", caseRef, caseUUID);

        return caseRef;
    }

    public String getCaseDataField(UUID caseUUID, String key) {
        log.debug("Looking up key {} for Case: {}", key, caseUUID);
        Map<String, String> dataMap = getCaseData(caseUUID).getDataMap(objectMapper);
        String value = dataMap.getOrDefault(key, null);
        log.debug("returning {} found value for Case: {}", value, caseUUID);
        return value;
    }

    public String getCaseType(UUID caseUUID) {
        String shortCode = caseUUID.toString().substring(34);
        log.debug("Looking up CaseType for Case: {} Shortcode: {}", caseUUID, shortCode);
        String caseType;
        try {
            CaseDataType caseDataType = infoClient.getCaseTypeByShortCode(shortCode);
            caseType = caseDataType.getDisplayCode();
        } catch (RestClientException e) {
            log.warn("Cannot determine type of caseUUID {} falling back to database lookup", caseUUID, value(EVENT, CASE_TYPE_LOOKUP_FAILED), value(EXCEPTION, e));
            caseType = getCaseData(caseUUID).getType();
        }
        log.debug("CaseType {} found for Case: {}", caseType, caseUUID);
        return caseType;
    }

    CaseData createCase(String caseType, Map<String, String> data, LocalDate dateReceived) {
        log.debug("Creating Case of type: {}", caseType);
        Long caseNumber = caseDataRepository.getNextSeriesId();
        log.debug("Allocating Ref: {}", caseNumber);
        CaseDataType caseDataType = infoClient.getCaseType(caseType);
        CaseData caseData = new CaseData(caseDataType, caseNumber, data, objectMapper, dateReceived);
        LocalDate deadline = infoClient.getCaseDeadline(caseType, dateReceived, 0);
        caseData.setCaseDeadline(deadline);
        LocalDate deadlineWarning = infoClient.getCaseDeadlineWarning(caseData.getType(), caseData.getDateReceived(), 0);
        caseData.setCaseDeadlineWarning(deadlineWarning);
        caseDataRepository.save(caseData);
        auditClient.createCaseAudit(caseData);
        log.info("Created Case: {} Ref: {} UUID: {}", caseData.getUuid(), caseData.getReference(), caseData.getUuid(), value(EVENT, CASE_CREATED));
        return caseData;
    }

    protected Map<String, String> calculateTotals(UUID caseUUID, UUID stageUUID, String listName) {
        log.debug("Calculating totals for Case: {} Stage: {}", caseUUID, stageUUID);
        Map<String, String> newDataMap = new HashMap<>();
        try {
            CaseData caseData = getCaseData(caseUUID);
            Map<String, String> dataMap = caseData.getDataMap(objectMapper);
            List<EntityDto<EntityTotalDto>> entityList = infoClient.getEntityListTotals(listName);
            for (EntityDto<EntityTotalDto> entityDto : entityList) {
                EntityTotalDto total = entityDto.getData();
                DataTotal dataTotal = new DataTotal();
                newDataMap.put(entityDto.getSimpleName(), dataTotal.calculate(dataMap, total.getAddFields(), total.getSubFields()).toString());
            }
            updateCaseData(caseUUID, stageUUID, newDataMap);
            log.info("Calculated totals for Case: {} Stage: {}", caseUUID, stageUUID, value(EVENT, CALCULATED_TOTALS));
        } catch (Exception e) {
            log.error("Failed to calculate totals for Case: {}", caseUUID, value(EVENT, CALCULATED_TOTALS), value(EXCEPTION, e));
        }
        return newDataMap;
    }

    protected void updateCaseData(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        log.debug("Updating data for Case: {}", caseUUID);
        if (data != null) {
            log.debug("Data size {}", data.size());
            CaseData caseData = getCaseData(caseUUID);
            caseData.update(data, objectMapper);
            caseDataRepository.save(caseData);
            auditClient.updateCaseAudit(caseData, stageUUID);
            log.info("Updated Case Data for Case: {} Stage: {}", caseUUID, stageUUID, value(EVENT, CASE_UPDATED));
        } else {
            log.warn("Data was null for Case: {} Stage: {}", caseUUID, stageUUID, value(EVENT, CASE_NOT_UPDATED_NULL_DATA));
        }
    }

    void updateDateReceived(UUID caseUUID, UUID stageUUID, LocalDate dateReceived, int days) {
        log.debug("Updating DateReceived for Case: {} Date: {}", caseUUID, dateReceived);
        CaseData caseData = getCaseData(caseUUID);
        if (dateReceived != null) {
            caseData.setDateReceived(dateReceived);
        }
        LocalDate deadline = infoClient.getCaseDeadline(caseData.getType(), caseData.getDateReceived(), days);
        caseData.setCaseDeadline(deadline);
        LocalDate deadlineWarning = infoClient.getCaseDeadlineWarning(caseData.getType(), caseData.getDateReceived(), days);
        if (deadlineWarning.isAfter(LocalDate.now())) {
            caseData.setCaseDeadlineWarning(deadlineWarning);
        }
        updateStageDeadlines(caseData);
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
    }

    private void updateStageDeadlines(CaseData caseData) {
        Map<String, String> dataMap = caseData.getDataMap(objectMapper);
        for (ActiveStage stage : caseData.getActiveStages()) {
            // Try and overwrite the deadlines with inputted values from the data map.
            String overrideDeadline = dataMap.get(String.format("%s_DEADLINE", stage.getStageType()));
            if (overrideDeadline == null) {
                LocalDate dateReceived = caseData.getDateReceived();
                LocalDate caseDeadline = caseData.getCaseDeadline();
                LocalDate caseDeadlineWarning = caseData.getCaseDeadlineWarning();
                LocalDate deadline = infoClient.getStageDeadline(stage.getStageType(), dateReceived, caseDeadline);
                stage.setDeadline(deadline);
                if (caseDeadlineWarning != null) {
                    LocalDate deadlineWarning = infoClient.getStageDeadlineWarning(stage.getStageType(), dateReceived, caseDeadlineWarning);
                    stage.setDeadlineWarning(deadlineWarning);
                }
            } else {
                LocalDate deadline = LocalDate.parse(overrideDeadline);
                stage.setDeadline(deadline);
            }
        }
    }

    void updateStageDeadline(UUID caseUUID, UUID stageUUID, String stageType, int days){
        log.debug("Updating deadline for Case: {} Stage: {} Days: {}", caseUUID, stageType, days);
        CaseData caseData = getCaseData(caseUUID);
        LocalDate deadline = infoClient.getCaseDeadline(caseData.getType(), caseData.getDateReceived(), days);
        Map<String, String> data = Map.of(String.format("%s_DEADLINE", stageType), deadline.toString());
        caseData.update(data, objectMapper);

        if (caseData.getActiveStages() != null) {
            ActiveStage activeStage = caseData.getActiveStages().stream().filter(stage -> stage.getStageType().equals(stageType)).findFirst().orElse(null);
            if (activeStage != null) {
                activeStage.setDeadline(deadline);
            }
        }

        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
        log.info("Updated Stage Deadline for Case: {} Stage: {} Days: {}", caseUUID, stageType, days, value(EVENT, STAGE_DEADLINE_UPDATED));
    }

    void updatePrimaryCorrespondent(UUID caseUUID, UUID stageUUID, UUID primaryCorrespondentUUID) {
        log.debug("Updating Primary Correspondent for Case: {} Correspondent: {}", caseUUID, primaryCorrespondentUUID);
        CaseData caseData = getCaseData(caseUUID);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
        log.info("Updated Primary Correspondent for Case: {} Correspondent: {}", caseUUID, primaryCorrespondentUUID, value(EVENT, PRIMARY_CORRESPONDENT_UPDATED));
    }

    void updatePrimaryTopic(UUID caseUUID, UUID stageUUID, UUID primaryTopicUUID) {
        log.debug("Updating Primary Topic for Case: {} Topic: {}", caseUUID, primaryTopicUUID);
        CaseData caseData = getCaseData(caseUUID);
        caseData.setPrimaryTopicUUID(primaryTopicUUID);
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
        log.info("Updated Primary Topic for Case: {} Correspondent: {}", caseUUID, primaryTopicUUID, value(EVENT, PRIMARY_TOPIC_UPDATED));
    }

    void completeCase(UUID caseUUID, boolean completed) {
        log.debug("Updating completed status Case: {} completed {}", caseUUID, completed);
        CaseData caseData = getCaseData(caseUUID);
        caseData.setCompleted(completed);
        caseDataRepository.save(caseData);
        auditClient.completeCaseAudit(caseData);
        log.info("Updated Case: {} completed {}", caseUUID, completed, value(EVENT, CASE_COMPLETED));
    }

    void deleteCase(UUID caseUUID, Boolean deleted) {
        log.debug("Deleting Case: {} flag: {}", caseUUID, deleted);
        CaseData caseData = getAnyCaseData(caseUUID);
        caseData.setDeleted(deleted);
        caseDataRepository.save(caseData);
        auditClient.deleteCaseAudit(caseData, deleted);
        auditClient.deleteAuditLinesForCase(caseUUID, UUID.randomUUID().toString(), deleted);
        log.info("Deleted Case: {} flag: {}", caseUUID, deleted, value(EVENT, CASE_DELETED));
    }

    CaseSummary getCaseSummary(UUID caseUUID) {
        log.debug("Building CaseSummary for Case: {}", caseUUID);

        CaseData caseData = getCaseData(caseUUID);
        caseData.getActiveStages();
        Set<FieldDto> summaryFields = infoClient.getCaseSummaryFields(caseData.getType());
        Map<String, String> caseDataMap = caseData.getDataMap(objectMapper);
        Set<AdditionalField> additionalFields = summaryFields.stream()
                .map(field -> new AdditionalField(field.getLabel(), caseDataMap.getOrDefault(field.getName(), ""), field.getComponent(), extractChoices(field)))
                .collect(Collectors.toSet());
        Map<String, LocalDate> stageDeadlinesOrig = infoClient.getStageDeadlines(caseData.getType(), caseData.getDateReceived());
        // Make a deep copy of the cached map so it isn't modified below
        Map<String, LocalDate> stageDeadlines = new HashMap<String, LocalDate>();
        for (Map.Entry<String, LocalDate> stageDeadline : stageDeadlinesOrig.entrySet()){
            stageDeadlines.put(stageDeadline.getKey(), stageDeadline.getValue());
        }
        // Try and overwrite the deadlines with inputted values from the data map.
        for (String stageType : stageDeadlines.keySet()) {
            String stageDeadlineKey = String.format("%s_DEADLINE", stageType);
            if (caseDataMap.containsKey(stageDeadlineKey)) {
                LocalDate deadline = LocalDate.parse(caseDataMap.get(stageDeadlineKey));
                stageDeadlines.put(stageType, deadline);
            }
        }

        log.info("Got Case Summary for Case: {} Ref: {}", caseData.getUuid(), caseData.getReference(), value(EVENT, CASE_SUMMARY_RETRIEVED));

        CaseSummary caseSummary = new CaseSummary(
                caseData.getCreated().toLocalDate(),
                caseData.getCaseDeadline(),
                stageDeadlines,
                additionalFields,
                caseData.getPrimaryCorrespondent(),
                caseData.getPrimaryTopic(),
                caseData.getActiveStages());
        auditClient.viewCaseSummaryAudit(caseData);
        return caseSummary;
    }

    private Object extractChoices(FieldDto fieldDto){
        if(fieldDto != null && fieldDto.getProps() != null && fieldDto.getProps() instanceof Map){
            Map propMap = (Map) fieldDto.getProps();
            return propMap.get("choices");
        }

        return null;
    }

    List<String> getDocumentTags(UUID caseUUID){
        String caseType = caseDataRepository.getCaseType(caseUUID);
        List<String> documentTags = infoClient.getDocumentTags(caseType);
        return documentTags;
    }

    Set<GetStandardLineResponse> getStandardLine(UUID caseUUID) {
        CaseData caseData = getCaseData(caseUUID);
        auditClient.viewStandardLineAudit(caseData);
        try {
            GetStandardLineResponse getStandardLineResponse = infoClient.getStandardLine(caseData.getPrimaryTopic().getTextUUID());
            return Set.of(getStandardLineResponse);
        } catch (HttpClientErrorException e) {
            return Set.of();
        }
    }

    List<TemplateDto> getTemplates(UUID caseUUID) {
        CaseData caseData = getCaseData(caseUUID);
        auditClient.viewTemplateAudit(caseData);
        try {
            return infoClient.getTemplates(caseData.getType());
        } catch (HttpClientErrorException e) {
            return List.of();
        }
    }

    Stream<TimelineItem> getCaseTimeline(UUID caseUUID) {
        log.debug("Building Timeline for Case: {}", caseUUID);

        CaseData caseData = getCaseData(caseUUID);
        Set<GetAuditResponse> audit = new HashSet<>();
        try {
            audit.addAll(auditClient.getAuditLinesForCase(caseUUID, TIMELINE_EVENTS));
            log.debug("Retrieved {} audit lines", audit.size());
        } catch (Exception e) {
            log.error("Failed to retrieve audit lines for case {}", caseUUID, value(EVENT, AUDIT_CLIENT_GET_AUDITS_FOR_CASE_FAILURE), value(EXCEPTION, e));
        }

        Set<CaseNote> notes = caseData.getCaseNotes();

        log.debug("Retrieved {} case notes", notes.size());

        Stream<TimelineItem> auditTimeline = audit.stream().map(a -> new TimelineItem(a.getCaseUUID(), a.getStageUUID(),
                a.getAuditTimestamp().toLocalDateTime(), a.getUserID(), a.getType(), a.getAuditPayload(), a.getUuid(),
                null, null));
        Stream<TimelineItem> notesTimeline = notes.stream().map(n -> {
            String auditPayload = "";
            try {
                auditPayload = objectMapper.writeValueAsString(new AuditPayload.CaseNote(n.getText()));
            } catch (JsonProcessingException e) {
                log.error("Failed to parse case note text for note {}", n.getUuid(), value(EVENT, UNCAUGHT_EXCEPTION), value(EXCEPTION, e));
            }
            return new TimelineItem(n.getCaseUUID(), null, n.getCreated(), n.getAuthor(), n.getCaseNoteType(),
                    auditPayload, n.getUuid(), n.getEdited(), n.getEditor());
        });

        return Stream.concat(auditTimeline, notesTimeline);
    }

    public Map<String, String> updateTeamByStageAndTexts(UUID caseUUID, UUID stageUUID, String stageType, String teamUUIDKey, String teamNameKey, String[] texts) {
        log.debug("Updating Team by Stage: {} {}", stageUUID, stageType);
        Map<String, String> dataMap = getCaseData(caseUUID).getDataMap(objectMapper);
        // build the linkValue text string used to search the team link table by converting "text" key to the case's data value
        String linkValue = null;
        for (String text : texts) {
            String value = dataMap.getOrDefault(text, "");
            if (!value.isEmpty()) {
                if (linkValue != null) {
                    linkValue += "_";
                    linkValue += value;
                } else {
                    linkValue = value;
                }
            }
        }

        TeamDto teamDto = infoClient.getTeamByStageAndText(stageType, linkValue);
        Map<String, String> teamMap = new HashMap<>();
        teamMap.put(teamUUIDKey, teamDto.getUuid().toString());
        teamMap.put(teamNameKey, teamDto.getDisplayName());

        return teamMap;
    }

    public Set<UUID> getCaseTeams(UUID caseUUID) {
        log.debug("Retrieving previous teams for : {}", caseUUID);

        Set<GetAuditResponse> auditLines = auditClient.getAuditLinesForCase(caseUUID, List.of(STAGE_ALLOCATED_TO_TEAM.toString(), STAGE_CREATED.toString()));
        log.info("Got {} audits", auditLines.size(), value(EVENT, AUDIT_CLIENT_GET_AUDITS_FOR_CASE_SUCCESS));

        return auditLines.stream().map(a -> {
            try {
                return objectMapper.readValue(a.getAuditPayload(), AuditPayload.StageAllocation.class).getAllocatedToUUID();
            } catch (IOException e) {
                log.error("Unable to parse audit payload for reason {}", e.getMessage(), value(EVENT, AUDIT_CLIENT_GET_AUDITS_FOR_CASE_FAILURE), value(EXCEPTION, e));
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public void clearCachedTemplateForCaseType(String caseType) {
        infoClient.clearCachedTemplateForCaseType(caseType);
    }
}
