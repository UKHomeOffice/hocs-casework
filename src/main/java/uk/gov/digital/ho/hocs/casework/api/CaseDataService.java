package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.api.factory.CaseCopyFactory;
import uk.gov.digital.ho.hocs.casework.api.factory.strategies.CaseCopyStrategy;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.AuditPayload;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.EntityDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.EntityTotalDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.domain.repository.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_TOPIC_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_TOPIC_DELETED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_UPDATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CORRESPONDENT_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CORRESPONDENT_DELETED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CORRESPONDENT_UPDATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.DOCUMENT_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.DOCUMENT_DELETED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_ALLOCATED_TO_TEAM;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_ALLOCATED_TO_USER;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_COMPLETED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_RECREATED;

@Service
@Slf4j
@Qualifier("CaseDataService")
public class CaseDataService {

    protected final CaseDataRepository caseDataRepository;
    protected final ActiveCaseViewDataRepository activeCaseViewDataRepository;
    protected final AuditClient auditClient;
    protected final ObjectMapper objectMapper;
    protected final InfoClient infoClient;
    protected final SomuItemRepository somuItemRepository;
    private final CaseCopyFactory caseCopyFactory;
    private final CaseLinkRepository caseLinkRepository;
    public static final Pattern CASE_REFERENCE_PATTERN = Pattern.compile("^[a-zA-Z0-9]{2,5}\\/([0-9]{7})\\/[0-9]{2}$");
    protected final CaseDeadlineExtensionTypeRepository caseDeadlineExtensionTypeRepository;

    @Autowired
    public CaseDataService(CaseDataRepository caseDataRepository, ActiveCaseViewDataRepository activeCaseViewDataRepository,
                           CaseLinkRepository caseLinkRepository, InfoClient infoClient,
                           ObjectMapper objectMapper, AuditClient auditClient, CaseCopyFactory caseCopyFactory, CaseDeadlineExtensionTypeRepository
                                   caseDeadlineExtensionTypeRepository, SomuItemRepository somuItemRepository) {

        this.caseDataRepository = caseDataRepository;
        this.activeCaseViewDataRepository = activeCaseViewDataRepository;
        this.caseLinkRepository = caseLinkRepository;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
        this.objectMapper = objectMapper;
        this.caseDeadlineExtensionTypeRepository = caseDeadlineExtensionTypeRepository;
        this.caseCopyFactory = caseCopyFactory;
        this.somuItemRepository = somuItemRepository;
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
            CORRESPONDENT_UPDATED.toString(),
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
        CaseData caseData = caseDataRepository.findActiveByUuid(caseUUID);
        if (caseData != null) {
            log.info("Got Case: {}", caseData.getUuid(), value(EVENT, CASE_RETRIEVED));
            return caseData;
        } else {
            log.error("Case: {}, not found!", caseUUID, value(EVENT, CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case: %s, not found!", caseUUID), CASE_NOT_FOUND);
        }
    }

    private ActiveCaseViewData getActiveCaseData(UUID caseUUID) {
        log.debug("Getting Case: {}", caseUUID);
        ActiveCaseViewData caseData = activeCaseViewDataRepository.findByUuid(caseUUID);
        if (caseData != null) {
            log.info("Got Case: {}", caseData.getUuid(), value(EVENT, CASE_RETRIEVED));
            return caseData;
        } else {
            log.error("Case: {}, not found!", caseUUID, value(EVENT, CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case: %s, not found!", caseUUID), CASE_NOT_FOUND);
        }
    }


    public CaseData getCaseDataByReference(String reference) {
        log.debug("Getting Case by reference: {}", reference);
        CaseData caseData = caseDataRepository.findByReference(reference);
        if (caseData != null) {
            log.info("Got Case by reference: {}", caseData.getUuid(), value(EVENT, CASE_RETRIEVED));
            return caseData;
        } else {
            log.error("Case: {}, not found!", reference, value(EVENT, CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case: %s, not found!", reference), CASE_NOT_FOUND);
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

    public String getCaseDataCaseRef(UUID caseUUID) {
        log.debug("Looking up CaseRef on all cases for Case: {}", caseUUID);
        String caseRef = caseDataRepository.getCaseDataCaseRef(caseUUID);
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

    CaseData createCase(String caseType, Map<String, String> data, LocalDate dateReceived, UUID fromCaseUUID) {
        log.debug("Creating Case of type: {}", caseType);

        CaseData caseData;
        if (fromCaseUUID == null) {
            caseData = createSimpleCase(caseType, data, dateReceived);
        } else {
            caseData = createCaseFromCaseUUID(caseType, data, dateReceived, fromCaseUUID);
        }

        auditClient.createCaseAudit(caseData);

        return caseData;
    }


    private CaseData createSimpleCase(String caseType, Map<String, String> data, LocalDate dateReceived) {

        Long caseNumber = caseDataRepository.getNextSeriesId();
        CaseData caseData = createCaseBaseData(caseType, data, dateReceived, caseNumber);
        log.info("Created Case: {} Ref: {} UUID: {}", caseData.getUuid(), caseData.getReference(), caseData.getUuid(), value(EVENT, CASE_CREATED));
        return caseData;
    }

    private CaseData createCaseFromCaseUUID(String caseType, Map<String, String> data, LocalDate dateReceived, UUID fromCaseUUID) {

        // does the previous case exist
        CaseData copyFromCase = getCaseData(fromCaseUUID);

        // get the existing case number
        Matcher findSerialNumber = CASE_REFERENCE_PATTERN.matcher(copyFromCase.getReference());
        if (!findSerialNumber.find()) {
            throw new ApplicationExceptions.EntityCreationException(String.format("Cannot extract case sequence number! Failed to create Case: %s", caseType), CASE_CREATE_FAILURE);
        }
        Long caseNumber = Long.parseLong(findSerialNumber.group(1));

        // create new case with previous serial number
        CaseData caseData = createCaseBaseData(caseType, data, dateReceived, caseNumber);

        // create a case link so that the 2 cases are related
        log.info("Creating link from case:{} to case:{}", fromCaseUUID, caseData.getUuid());
        caseLinkRepository.save(new CaseLink(fromCaseUUID, caseData.getUuid()));

        // copy the previous case details into the new case
        Optional<CaseCopyStrategy> strategy = caseCopyFactory.getStrategy(copyFromCase.getType(), caseType);

        if (strategy.isPresent()) {
            strategy.get().copyCase(copyFromCase, caseData);
        } else {
            throw new ApplicationExceptions.EntityCreationException(String.format("Cannot find a copy strategy from:%s to :%s", copyFromCase.getType(), caseType), CASE_CREATE_FAILURE);
        }

        log.info("Created Case: {} Ref: {} UUID: {}", caseData.getUuid(), caseData.getReference(), caseData.getUuid(), value(EVENT, CASE_CREATED));
        return caseData;
    }

    private CaseData createCaseBaseData(String caseType, Map<String, String> data, LocalDate dateReceived, Long caseNumber) {
        log.debug("Allocating Ref: {}", caseNumber);
        CaseDataType caseDataType = infoClient.getCaseType(caseType);
        CaseData caseData = new CaseData(caseDataType, caseNumber, data, objectMapper, dateReceived);
        LocalDate deadline = infoClient.getCaseDeadline(caseType, dateReceived, 0);
        caseData.setCaseDeadline(deadline);
        LocalDate deadlineWarning = infoClient.getCaseDeadlineWarning(caseData.getType(), caseData.getDateReceived(), 0);
        caseData.setCaseDeadlineWarning(deadlineWarning);
        caseDataRepository.save(caseData);

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

    public void applyExtension(UUID caseUUID, UUID stageUUID, String type, String note) {
        log.debug("Applying extension for Case: {} Extension: {}", caseUUID, type);
        CaseData caseData = getCaseData(caseUUID);

        CaseDeadlineExtensionType caseDeadlineExtensionType =
                caseDeadlineExtensionTypeRepository.findById(type).orElseThrow();

        log.debug("Got extension type: {}", caseDeadlineExtensionType.getType());

        final CaseDeadlineExtension caseDeadlineExtension =
                caseData.addDeadlineExtension(caseDeadlineExtensionType, note);

        int extensionDays = calculateExtensionDays(caseData.getDeadlineExtensions());

        LocalDate deadline = infoClient.getCaseDeadline(
                caseData.getType(),
                caseData.getDateReceived(),
                0,
                extensionDays);

        caseData.setCaseDeadline(deadline);
        caseData.setCaseDeadlineWarning(deadline.minusDays(2));

        caseDataRepository.save(caseData);

        updateStageDeadlines(caseData);
        auditClient.createExtensionAudit(caseDeadlineExtension);
    }

    private static int calculateExtensionDays(Set<CaseDeadlineExtension> caseDeadlineExtensions) {
        return caseDeadlineExtensions.stream()
                .map(e -> e.getCaseDeadlineExtensionType().getWorkingDays()).reduce(0, Integer::sum);
    }

    public void updateCaseData(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
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

        Assert.notNull(caseUUID, "Case UUID is null");
        Assert.notNull(stageUUID, "Stage UUID is null");

        log.debug("Updating DateReceived for Case: {} Date: {}", caseUUID, dateReceived);
        CaseData caseData = getCaseData(caseUUID);
        if (dateReceived != null) {
            caseData.setDateReceived(dateReceived);
        }
        LocalDate deadline = infoClient.getCaseDeadline(caseData.getType(), caseData.getDateReceived(), days);
        caseData.setCaseDeadline(deadline);

        updateCaseDeadlines(caseData, stageUUID, dateReceived, days);
    }

    void updateDispatchDeadlineDate(UUID caseUUID, UUID stageUUID, LocalDate dispatchDeadlineDate) {

        Assert.notNull(caseUUID, "Case UUID is null");
        Assert.notNull(stageUUID, "Stage UUID is null");

        log.debug("Updating dispatch deadline date for Case: {} Stage: {} Date: {}", caseUUID, stageUUID, dispatchDeadlineDate);
        CaseData caseData = getCaseData(caseUUID);
        if (dispatchDeadlineDate != null) {
            caseData.setCaseDeadline(dispatchDeadlineDate);
        }

        updateCaseDeadlines(caseData, stageUUID, dispatchDeadlineDate, 0);
    }


    private void updateCaseDeadlines(CaseData caseData, UUID stageUUID, LocalDate dateReceived, int days) {
        log.debug("Updating case deadlines for Case: {} Date: {}", caseData.getUuid(), dateReceived);

        LocalDate deadlineWarning = infoClient.getCaseDeadlineWarning(caseData.getType(), caseData.getDateReceived(), days);
        if (deadlineWarning.isAfter(LocalDate.now())) {
            caseData.setCaseDeadlineWarning(deadlineWarning);
        }
        updateStageDeadlines(caseData);
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
    }

    private void updateStageDeadlines(CaseData caseData) {

        if (caseData.getActiveStages() == null) {
            log.warn("Case uuid:{} supplied with null active stages", caseData.getUuid());
            return;
        }

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

    void updateStageDeadline(UUID caseUUID, UUID stageUUID, String stageType, int days) {
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

    void updateDeadlineForStages(UUID caseUUID, UUID stageUUID, Map<String, Integer> stageTypeAndDaysMap) {

        CaseData caseData = getCaseData(caseUUID);

        stageTypeAndDaysMap.forEach(
                (stageType, noOfDays) -> {
                    log.debug("Updating deadline for Case: {} Stage: {} Days: {}", caseUUID, stageType, noOfDays);
                    LocalDate deadline = infoClient.getCaseDeadline(
                            caseData.getType(),
                            caseData.getDateReceived(),
                            noOfDays
                    );
                    Map<String, String> data = Map.of(String.format("%s_DEADLINE", stageType), deadline.toString());
                    caseData.update(data, objectMapper);

                    if (caseData.getActiveStages() != null) {
                        ActiveStage activeStage = caseData.getActiveStages().stream().filter(
                                stage -> stage.getStageType().equals(stageType)
                        ).findFirst().orElse(null);
                        if (activeStage != null) {
                            activeStage.setDeadline(deadline);
                        }
                    }
                }
        );

        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
        log.info("Updated {} stage deadlines for Case: {}", caseUUID, stageTypeAndDaysMap.size(), value(EVENT, STAGE_DEADLINE_UPDATED));
    }

    void updatePrimaryCorrespondent(UUID caseUUID, UUID stageUUID, UUID primaryCorrespondentUUID) {
        log.debug("Updating Primary Correspondent for Case: {} Correspondent: {}", caseUUID, primaryCorrespondentUUID);
        CaseData caseData = getCaseData(caseUUID);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
        log.info("Updated Primary Correspondent for Case: {} Correspondent: {}", caseUUID, primaryCorrespondentUUID, value(EVENT, PRIMARY_CORRESPONDENT_UPDATED));
    }

    void updatePrimaryTopic(UUID caseUUID, UUID stageUUID, UUID primaryTopicUUID, UUID textUUID) {
        log.debug("Updating Primary Topic for Case: {} Topic: {}", caseUUID, primaryTopicUUID);
        CaseData caseData = getCaseData(caseUUID);
        caseData.setPrimaryTopicUUID(primaryTopicUUID);
        // If we have been passed a text UUID we should change the data to replace it in data with the topic UUID
        // to allow
        if (textUUID != null){
            if(caseData.getData().contains(textUUID.toString())){
                caseData.setData(StringUtils.replace(caseData.getData(),
                        textUUID.toString(), primaryTopicUUID.toString()));
            }
        }
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
        log.info("Updated Primary Topic for Case: {} Correspondent: {}", caseUUID, primaryTopicUUID, value(EVENT, PRIMARY_TOPIC_UPDATED));
    }

    void completeCase(UUID caseUUID, boolean completed) {
        log.debug("Updating completed status Case: {} completed {}", caseUUID, completed);
        CaseData caseData = getCaseData(caseUUID);
        caseData.setCompleted(completed);
        if (completed) {
            caseData.update(Map.of(CaseworkConstants.CURRENT_STAGE, ""), objectMapper);
        }
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, null);
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

    CaseSummary getCaseSummary(final UUID caseUUID) {
        log.debug("Building CaseSummary for Case: {}", caseUUID);

        final CaseData caseData = getCaseData(caseUUID);

        CaseSummary.Builder summaryBuilder = new CaseSummary.Builder();

        summaryBuilder
                .withCaseType(caseData.getType())
                .withCreatedDate(caseData.getCreated().toLocalDate())
                .withCaseDeadline(caseData.getCaseDeadline())
                .withPrimaryCorrespondent(caseData.getPrimaryCorrespondent())
                .withPrimaryTopic(caseData.getPrimaryTopic())
                .withActiveStages(caseData.getActiveStages());

        Set<FieldDto> summaryFields = infoClient.getCaseSummaryFields(caseData.getType());
        Map<String, String> caseDataMap = caseData.getDataMap(objectMapper);

        summaryBuilder.withAdditionalFields(getAdditionalFieldsForSummary(summaryFields, caseDataMap));
        summaryBuilder.withStageDeadlines(getStageDeadlines(caseData, caseDataMap));
        summaryBuilder.withDeadlineExtensions(getCaseDeadlineExtensions(caseData));

        final Map<UUID, SomuTypeDto> eligibleSomuTypesByUuid =
                infoClient.getAllSomuTypesForCaseType(caseData.getType())
                        .stream()
                        .filter(SomuTypeDto::isActive)
                        .filter(type ->
                                type.getSchema().getOrDefault("showInSummary", false).equals(true)
                        )
                        .collect(Collectors.toMap(SomuTypeDto::getUuid, Function.identity()));

        eligibleSomuTypesByUuid.values()
                .forEach(
                        somuType -> somuItemRepository.findByCaseUuidAndSomuUuid(caseUUID, somuType.getUuid()).forEach(
                                somuItem -> {
                                    try {
                                        summaryBuilder.addSomuItem(somuType, somuItem.getData());
                                    } catch (JsonProcessingException e) {
                                        log.error("Error parsing somu item in summary for " +
                                                        "Case: {} Ref: {} Somu Item UUID: {}",
                                                caseData.getUuid(), caseData.getReference(), somuItem.getUuid(),
                                                value(EVENT, CASE_SUMMARY_CANNOT_PARSE_SOMU_ITEM));
                                    }
                                }
                        )
                );

        auditClient.viewCaseSummaryAudit(caseData);

        final ActiveCaseViewData activeCaseViewData = getActiveCaseData(caseUUID);
        final CaseSummary caseSummary = summaryBuilder
                .withPreviousCaseReference(activeCaseViewData.getPreviousCaseReference())
                .withPreviousCaseUUID(activeCaseViewData.getPreviousCaseUUID())
                .withPreviousCaseStageUUID(activeCaseViewData.getPreviousCaseStageUUID())
                .build();

        log.info("Got Case Summary for Case: {} Ref: {}", caseData.getUuid(), caseData.getReference(), value(EVENT, CASE_SUMMARY_RETRIEVED));

        return caseSummary;
    }

    private Map<String, Integer> getCaseDeadlineExtensions(CaseData caseData) {
        return Objects.isNull(caseData.getDeadlineExtensions()) ? Collections.emptyMap() :
                caseData.getDeadlineExtensions().stream().collect(Collectors.toMap(
                        e -> e.getCaseDeadlineExtensionType().getType(), e -> e.getCaseDeadlineExtensionType().getWorkingDays()));
    }

    private Map<String, LocalDate> getStageDeadlines(CaseData caseData, Map<String, String> caseDataMap) {
        Map<String, LocalDate> stageDeadlinesOrig = infoClient.getStageDeadlines(caseData.getType(), caseData.getDateReceived());
        // Make a deep copy of the cached map so it isn't modified below
        Map<String, LocalDate> stageDeadlines = new LinkedHashMap<String, LocalDate>();
        for (Map.Entry<String, LocalDate> stageDeadline : stageDeadlinesOrig.entrySet()) {
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
        return stageDeadlines;
    }

    private Set<AdditionalField> getAdditionalFieldsForSummary(Set<FieldDto> summaryFields, Map<String, String> caseDataMap) {
        Set<AdditionalField> additionalFields = summaryFields.stream()
                .map(field -> new AdditionalField(field.getLabel(), caseDataMap.getOrDefault(field.getName(), ""), field.getComponent(), extractChoices(field)))
                .collect(Collectors.toSet());
        return additionalFields;
    }

    private Object extractChoices(FieldDto fieldDto) {
        if (fieldDto != null && fieldDto.getProps() != null && fieldDto.getProps() instanceof Map) {
            Map propMap = (Map) fieldDto.getProps();
            return propMap.get("choices");
        }

        return null;
    }

    List<String> getDocumentTags(UUID caseUUID) {
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
