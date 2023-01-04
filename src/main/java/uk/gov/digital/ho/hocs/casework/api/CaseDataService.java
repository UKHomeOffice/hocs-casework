package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.GetStandardLineResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.MigrateCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.StageTypeDto;
import uk.gov.digital.ho.hocs.casework.api.dto.TemplateDto;
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
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveCaseViewData;
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseLink;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseSummary;
import uk.gov.digital.ho.hocs.casework.domain.model.DataTotal;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.model.TimelineItem;
import uk.gov.digital.ho.hocs.casework.domain.repository.ActiveCaseViewDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseLinkRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.AUDIT_CLIENT_GET_AUDITS_FOR_CASE_FAILURE;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.AUDIT_CLIENT_GET_AUDITS_FOR_CASE_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CALCULATED_TOTALS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_COMPLETED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_CREATE_FAILURE;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_DELETED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_NOT_FOUND;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_NOT_UPDATED_NULL_DATA;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_RETRIEVED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_TYPE_LOOKUP_FAILED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.DATA_MAPPING_EXCEPTION;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.DATA_MAPPING_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EXCEPTION;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.GET_CASE_REF_BY_UUID;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.GET_CASE_REF_BY_UUID_FAILURE;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.PRIMARY_CORRESPONDENT_UPDATED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.PRIMARY_TOPIC_UPDATED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_CREATED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_DEADLINE_UPDATED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.UNCAUGHT_EXCEPTION;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.APPEAL_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.APPEAL_UPDATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_TOPIC_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_TOPIC_DELETED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_UPDATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CORRESPONDENT_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CORRESPONDENT_DELETED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CORRESPONDENT_UPDATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.DATA_FIELD_UPDATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.DOCUMENT_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.DOCUMENT_DELETED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.EXTENSION_APPLIED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.EXTERNAL_INTEREST_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.EXTERNAL_INTEREST_UPDATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_ALLOCATED_TO_TEAM;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_ALLOCATED_TO_USER;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_COMPLETED;
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

    private final CaseCopyFactory caseCopyFactory;

    private final CaseLinkRepository caseLinkRepository;

    private final CaseActionService caseActionService;

    private final DeadlineService deadlineService;

    private final StageRepository stageRepository;

    private final CaseDataSummaryService caseDataSummaryService;

    public static final Pattern CASE_REFERENCE_PATTERN = Pattern.compile("^[a-zA-Z0-9]{2,5}\\/([0-9]{7})\\/[0-9]{2}$");

    public static final Pattern CASE_UUID_PATTERN = Pattern.compile(
        "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b", Pattern.CASE_INSENSITIVE);

    public static final String REFERENCE_NOT_FOUND = "REFERENCE NOT FOUND";

    @Autowired
    public CaseDataService(CaseDataRepository caseDataRepository,
                           ActiveCaseViewDataRepository activeCaseViewDataRepository,
                           CaseLinkRepository caseLinkRepository,
                           InfoClient infoClient,
                           ObjectMapper objectMapper,
                           AuditClient auditClient,
                           CaseCopyFactory caseCopyFactory,
                           CaseActionService caseActionService,
                           DeadlineService deadlineService,
                           StageRepository stageRepository,
                           CaseDataSummaryService caseDataSummaryService) {

        this.caseDataRepository = caseDataRepository;
        this.activeCaseViewDataRepository = activeCaseViewDataRepository;
        this.caseLinkRepository = caseLinkRepository;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
        this.objectMapper = objectMapper;
        this.caseCopyFactory = caseCopyFactory;
        this.caseActionService = caseActionService;
        this.deadlineService = deadlineService;
        this.stageRepository = stageRepository;
        this.caseDataSummaryService = caseDataSummaryService;
    }

    public static final List<String> TIMELINE_EVENTS = List.of(CASE_CREATED.toString(), CASE_COMPLETED.toString(),
        CASE_TOPIC_CREATED.toString(), CASE_TOPIC_DELETED.toString(), STAGE_ALLOCATED_TO_TEAM.toString(),
        STAGE_CREATED.toString(), STAGE_RECREATED.toString(), STAGE_COMPLETED.toString(),
        STAGE_ALLOCATED_TO_USER.toString(), CORRESPONDENT_DELETED.toString(), CORRESPONDENT_CREATED.toString(),
        CORRESPONDENT_UPDATED.toString(), DOCUMENT_CREATED.toString(), DOCUMENT_DELETED.toString(),
        APPEAL_UPDATED.toString(), APPEAL_CREATED.toString(), EXTENSION_APPLIED.toString(),
        EXTERNAL_INTEREST_CREATED.toString(), EXTERNAL_INTEREST_UPDATED.toString(), DATA_FIELD_UPDATED.toString()

                                                              );

    public CaseData getCase(UUID caseUUID) {
        CaseData caseData = getCaseData(caseUUID);
        auditClient.viewCaseAudit(caseData);
        return caseData;
    }

    protected CaseData getCaseData(UUID caseUUID) {
        log.debug("Getting Case: {}", caseUUID);
        CaseData caseData = caseDataRepository.findActiveByUuid(caseUUID);
        if (caseData==null) {
            log.error("Case: {}, not found!", caseUUID, value(EVENT, CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case: %s, not found!", caseUUID),
                CASE_NOT_FOUND);
        }
        log.info("Got Case: {}", caseData.getUuid(), value(EVENT, CASE_RETRIEVED));
        return caseData;
    }

    private ActiveCaseViewData getActiveCaseData(UUID caseUUID) {
        log.debug("Getting Case: {}", caseUUID);
        ActiveCaseViewData caseData = activeCaseViewDataRepository.findByUuid(caseUUID);
        if (caseData==null) {
            log.error("Case: {}, not found!", caseUUID, value(EVENT, CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case: %s, not found!", caseUUID),
                CASE_NOT_FOUND);
        }
        log.info("Got Case: {}", caseData.getUuid(), value(EVENT, CASE_RETRIEVED));
        return caseData;
    }

    public CaseData getCaseDataByReference(String reference) {
        log.debug("Getting Case by reference: {}", reference);
        CaseData caseData = caseDataRepository.findByReference(reference);
        if (caseData==null) {
            log.error("Case: {}, not found!", reference, value(EVENT, CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case: %s, not found!", reference),
                CASE_NOT_FOUND);
        }
        log.info("Got Case by reference: {}", caseData.getUuid(), value(EVENT, CASE_RETRIEVED));
        return caseData;
    }

    private CaseData getAnyCaseData(UUID caseUUID) {
        log.debug("Getting any Case: {}", caseUUID);
        CaseData caseData = caseDataRepository.findAnyByUuid(caseUUID);
        if (caseData==null) {
            log.error("Any Case: {}, not found!", caseUUID, value(EVENT, CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Any Case: %s, not found!", caseUUID),
                CASE_NOT_FOUND);
        }
        log.info("Got any Case: {}", caseData.getUuid(), value(EVENT, CASE_RETRIEVED));
        return caseData;
    }

    public String getCaseRef(UUID caseUUID) {
        log.info("Looking up CaseRef for Case: {}", caseUUID, value(EVENT, GET_CASE_REF_BY_UUID));

        CaseData caseData = caseDataRepository.findActiveByUuid(caseUUID);
        if (caseData==null) {
            log.warn("CaseData not found for Case: {}", caseUUID, value(EVENT, GET_CASE_REF_BY_UUID_FAILURE));
            return REFERENCE_NOT_FOUND;
        }
        return caseData.getReference();
    }

    public String getCaseDataField(UUID caseUUID, String key) {
        return getCaseDataField(getCaseData(caseUUID), key);
    }

    public String getCaseDataField(CaseData caseData, String key) {
        log.debug("Looking up key {} for Case: {}", key, caseData.getUuid());
        return getCaseData(caseData.getUuid()).getData(key);
    }

    public String getCaseType(UUID caseUUID) {
        String shortCode = caseUUID.toString().substring(34);
        log.debug("Looking up CaseType for Case: {} Shortcode: {}", caseUUID, shortCode);
        String caseType;
        try {
            CaseDataType caseDataType = infoClient.getCaseTypeByShortCode(shortCode);
            caseType = caseDataType.getDisplayCode();
        } catch (RestClientException e) {
            log.warn("Cannot determine type of caseUUID {} falling back to database lookup", caseUUID,
                value(EVENT, CASE_TYPE_LOOKUP_FAILED), value(EXCEPTION, e));
            caseType = getCaseData(caseUUID).getType();
        }
        log.debug("CaseType {} found for Case: {}", caseType, caseUUID);
        return caseType;
    }

    CaseData createCase(String caseType, Map<String, String> data, LocalDate dateReceived, UUID fromCaseUUID) {
        log.debug("Creating Case of type: {}", caseType);

        CaseData caseData;
        if (fromCaseUUID==null) {
            caseData = createSimpleCase(caseType, data, dateReceived);
        } else {
            caseData = createCaseFromCaseUUID(caseType, data, dateReceived, fromCaseUUID);
        }

        auditClient.createCaseAudit(caseData);

        return caseData;
    }

    public MigrateCaseResponse migrateCase(String caseType, UUID fromCaseUUID) {
        log.debug("Migrating Case of type: {}", caseType);

        CaseData caseData;
        caseData = migrateCaseFromCaseUUID(caseType, fromCaseUUID);

        auditClient.migrateCaseAudit(caseData);

        return new MigrateCaseResponse(caseData.getUuid(), caseData.getDataMap());
    }

    private CaseData createSimpleCase(String caseType, Map<String, String> data, LocalDate dateReceived) {

        Long caseNumber = caseDataRepository.getNextSeriesId();
        CaseData caseData = createCaseBaseData(caseType, data, dateReceived, caseNumber);
        log.info("Created Case: {} Ref: {} UUID: {}", caseData.getUuid(), caseData.getReference(), caseData.getUuid(),
            value(EVENT, CASE_CREATED));
        return caseData;
    }

    private CaseData createCaseFromCaseUUID(String caseType,
                                            Map<String, String> data,
                                            LocalDate dateReceived,
                                            UUID fromCaseUUID) {

        // does the previous case exist
        CaseData copyFromCase = getCaseData(fromCaseUUID);

        return createCaseFromCaseUUID(caseType, data, dateReceived, fromCaseUUID, copyFromCase);
    }

    private CaseData migrateCaseFromCaseUUID(String caseType, UUID fromCaseUUID) {

        CaseData copyFromCase = getCaseData(fromCaseUUID);
        Map<String, String> data = copyFromCase.getDataMap();
        LocalDate dateReceived = copyFromCase.getDateReceived();

        return createCaseFromCaseUUID(caseType, data, dateReceived, fromCaseUUID, copyFromCase);
    }

    private CaseData createCaseFromCaseUUID(String caseType,
                                            Map<String, String> data,
                                            LocalDate dateReceived,
                                            UUID fromCaseUUID,
                                            CaseData copyFromCase) {
        // get the existing case number
        Matcher findSerialNumber = CASE_REFERENCE_PATTERN.matcher(copyFromCase.getReference());
        if (!findSerialNumber.find()) {
            throw new ApplicationExceptions.EntityCreationException(
                String.format("Cannot extract case sequence number! Failed to create Case: %s", caseType),
                CASE_CREATE_FAILURE);
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
            throw new ApplicationExceptions.EntityCreationException(
                String.format("Cannot find a copy strategy from:%s to :%s", copyFromCase.getType(), caseType),
                CASE_CREATE_FAILURE);
        }

        log.info("Created Case: {} Ref: {} UUID: {}", caseData.getUuid(), caseData.getReference(), caseData.getUuid(),
            value(EVENT, CASE_CREATED));
        return caseData;
    }

    private CaseData createCaseBaseData(String caseType,
                                        Map<String, String> data,
                                        LocalDate dateReceived,
                                        Long caseNumber) {
        log.debug("Allocating Ref: {}", caseNumber);
        CaseDataType caseDataType = infoClient.getCaseType(caseType);
        CaseData caseData = new CaseData(caseDataType, caseNumber, data, dateReceived);
        LocalDate deadline = deadlineService.calculateWorkingDaysForCaseType(caseType, dateReceived,
            caseDataType.getSla());

        caseData.setCaseDeadline(deadline);
        LocalDate deadlineWarning = deadlineService.calculateWorkingDaysForCaseType(caseType,
            caseData.getDateReceived(), caseDataType.getDeadLineWarning());

        caseData.setCaseDeadlineWarning(deadlineWarning);
        caseDataRepository.save(caseData);

        return caseData;
    }

    protected Map<String, String> calculateTotals(UUID caseUUID, UUID stageUUID, String listName) {
        log.debug("Calculating totals for Case: {} Stage: {}", caseUUID, stageUUID);
        Map<String, String> newDataMap = new HashMap<>();
        try {
            Map<String, String> dataMap = getCaseData(caseUUID).getDataMap();
            List<EntityDto<EntityTotalDto>> entityList = infoClient.getEntityListTotals(listName);
            for (EntityDto<EntityTotalDto> entityDto : entityList) {
                EntityTotalDto total = entityDto.getData();
                DataTotal dataTotal = new DataTotal();
                newDataMap.put(entityDto.getSimpleName(),
                    dataTotal.calculate(dataMap, total.getAddFields(), total.getSubFields()).toString());
            }
            updateCaseData(caseUUID, stageUUID, newDataMap);
            log.info("Calculated totals for Case: {} Stage: {}", caseUUID, stageUUID, value(EVENT, CALCULATED_TOTALS));
        } catch (Exception e) {
            log.error("Failed to calculate totals for Case: {}", caseUUID, value(EVENT, CALCULATED_TOTALS),
                value(EXCEPTION, e));
        }
        return newDataMap;
    }

    public void updateCaseData(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        if (data==null) {
            log.warn("Data was null for Case: {} Stage: {}", caseUUID, stageUUID,
                value(EVENT, CASE_NOT_UPDATED_NULL_DATA));
            return;
        }
        updateCaseData(getCaseData(caseUUID), stageUUID, data);
    }

    public void updateCaseData(CaseData caseData, UUID stageUUID, Map<String, String> data) {
        log.debug("Updating data for Case: {}", caseData.getUuid());
        if (data==null) {
            log.warn("Data was null for Case: {} Stage: {}", caseData.getUuid(), stageUUID,
                value(EVENT, CASE_NOT_UPDATED_NULL_DATA));
            return;
        }

        log.debug("Data size {}", data.size());
        caseData.update(data);
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
        log.info("Updated Case Data for Case: {} Stage: {}", caseData.getUuid(), stageUUID, value(EVENT, CASE_UPDATED));
    }

    void updateDateReceived_defaultSla(UUID caseUUID, UUID stageUUID, LocalDate dateReceived) {
        Assert.notNull(caseUUID, "Case UUID is null");
        Assert.notNull(stageUUID, "Stage UUID is null");

        log.debug("Updating DateReceived for Case: {} Date: {}", caseUUID, dateReceived);
        CaseData caseData = getCaseData(caseUUID);
        caseData.setDateReceived(dateReceived);

        CaseDataType caseDataType = infoClient.getCaseType(caseData.getType());
        LocalDate deadline = deadlineService.calculateWorkingDaysForCaseType(caseData.getType(),
            caseData.getDateReceived(), caseDataType.getSla());

        caseData.setCaseDeadline(deadline);

        updateCaseDeadlines(caseData, stageUUID);
    }

    void overrideSla(UUID caseUUID, UUID stageUUID, int days) {
        Assert.notNull(caseUUID, "Case UUID is null");
        Assert.notNull(stageUUID, "Stage UUID is null");

        log.debug("Overriding SLA for Case: {} with new SLA: {}", caseUUID, days);
        CaseData caseData = getCaseData(caseUUID);

        LocalDate deadline = deadlineService.calculateWorkingDaysForCaseType(caseData.getType(),
            caseData.getDateReceived(), days);

        caseData.setCaseDeadline(deadline);

        updateCaseDeadlines(caseData, stageUUID, days);
    }

    void updateDispatchDeadlineDate(UUID caseUUID, UUID stageUUID, LocalDate dispatchDeadlineDate) {

        Assert.notNull(caseUUID, "Case UUID is null");
        Assert.notNull(stageUUID, "Stage UUID is null");

        log.debug("Updating dispatch deadline date for Case: {} Stage: {} Date: {}", caseUUID, stageUUID,
            dispatchDeadlineDate);
        CaseData caseData = getCaseData(caseUUID);
        if (dispatchDeadlineDate!=null) {
            caseData.setCaseDeadline(dispatchDeadlineDate);
        }

        updateCaseDeadlines(caseData, stageUUID);
    }

    private void updateCaseDeadlines(CaseData caseData, UUID stageUUID) {
        log.debug("Updating case deadlines for Case: {} Date: {}", caseData.getUuid(), caseData.getDateReceived());

        final CaseDataType caseTypeDto = infoClient.getCaseType(caseData.getType());
        LocalDate deadlineWarning = deadlineService.calculateWorkingDaysForCaseType(caseData.getType(),
            caseData.getDateReceived(), caseTypeDto.getDeadLineWarning());

        if (deadlineWarning.isAfter(LocalDate.now())) {
            caseData.setCaseDeadlineWarning(deadlineWarning);
        }

        updateStageDeadlines(caseData);
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
    }

    private void updateCaseDeadlines(CaseData caseData, UUID stageUUID, int days) {
        log.debug("Updating case deadlines for Case: {} Date: {}", caseData.getUuid(), caseData.getDateReceived());

        LocalDate deadlineWarning = deadlineService.calculateWorkingDaysForCaseType(caseData.getType(),
            caseData.getDateReceived(), days);

        if (deadlineWarning.isAfter(LocalDate.now())) {
            caseData.setCaseDeadlineWarning(deadlineWarning);
        }

        updateStageDeadlines(caseData);
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
    }

    public void updateStageDeadlines(CaseData caseData) {

        if (caseData.getActiveStages()==null) {
            log.warn("Case uuid:{} supplied with null active stages", caseData.getUuid());
            return;
        }

        final Set<StageTypeDto> stageTypesForCaseType = infoClient.getAllStagesForCaseType(caseData.getType());

        for (ActiveStage stage : caseData.getActiveStages()) {
            // Try and overwrite the deadlines with inputted values from the data map.
            String overrideDeadline = caseData.getData(String.format("%s_DEADLINE", stage.getStageType()));
            if (overrideDeadline==null) {
                LocalDate dateReceived = caseData.getDateReceived();
                LocalDate caseDeadline = caseData.getCaseDeadline();
                LocalDate caseDeadlineWarning = caseData.getCaseDeadlineWarning();

                final StageTypeDto stageDefinition = stageTypesForCaseType.stream().filter(
                    element -> element.getType().equals(stage.getStageType())).collect(Collectors.toList()).get(0);

                LocalDate deadline = deadlineService.calculateWorkingDaysForStage(caseData.getType(),
                    caseData.getDateReceived(), caseData.getCaseDeadline(), stageDefinition.getSla());

                stage.setDeadline(deadline);
                if (caseDeadlineWarning!=null) {
                    LocalDate deadlineWarning = deadlineService.calculateWorkingDaysForStage(caseData.getType(),
                        dateReceived, caseDeadline, stageDefinition.getDeadlineWarning());

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
        updateDeadlineForStage(caseData, stageType, days);

        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
        log.info("Updated Stage Deadline for Case: {} Stage: {} Days: {}", caseUUID, stageType, days,
            value(EVENT, STAGE_DEADLINE_UPDATED));
    }

    void updateDeadlineForStages(UUID caseUUID, UUID stageUUID, Map<String, Integer> stageTypeAndDaysMap) {

        CaseData caseData = getCaseData(caseUUID);

        stageTypeAndDaysMap.forEach((stageType, noOfDays) -> {
            log.debug("Updating deadline for Case: {} Stage: {} Days: {}", caseUUID, stageType, noOfDays);
            updateDeadlineForStage(caseData, stageType, noOfDays);
        });

        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
        log.info("Updated {} stage deadlines for Case: {}", caseUUID, stageTypeAndDaysMap.size(),
            value(EVENT, STAGE_DEADLINE_UPDATED));
    }

    void updatePrimaryCorrespondent(UUID caseUUID, UUID stageUUID, UUID primaryCorrespondentUUID) {
        log.debug("Updating Primary Correspondent for Case: {} Correspondent: {}", caseUUID, primaryCorrespondentUUID);
        CaseData caseData = getCaseData(caseUUID);
        caseData.setPrimaryCorrespondentUUID(primaryCorrespondentUUID);
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
        log.info("Updated Primary Correspondent for Case: {} Correspondent: {}", caseUUID, primaryCorrespondentUUID,
            value(EVENT, PRIMARY_CORRESPONDENT_UPDATED));
    }

    void updatePrimaryTopic(UUID caseUUID, UUID stageUUID, UUID primaryTopicUUID) {
        log.debug("Updating Primary Topic for Case: {} Topic: {}", caseUUID, primaryTopicUUID);
        CaseData caseData = getCaseData(caseUUID);
        caseData.setPrimaryTopicUUID(primaryTopicUUID);
        caseDataRepository.save(caseData);
        auditClient.updateCaseAudit(caseData, stageUUID);
        log.info("Updated Primary Topic for Case: {} Correspondent: {}", caseUUID, primaryTopicUUID,
            value(EVENT, PRIMARY_TOPIC_UPDATED));
    }

    void completeCase(UUID caseUUID, boolean completed) {
        log.debug("Updating completed status Case: {} completed {}", caseUUID, completed);
        CaseData caseData = getCaseData(caseUUID);
        caseData.setCompleted(completed);

        // Complete final stage if active stage exists
        Optional<Stage> maybeFinalStage = stageRepository.findFirstByTeamUUIDIsNotNullAndCaseUUID(caseUUID);
        maybeFinalStage.ifPresent(stage -> {
            stage.setTeam(null);
            stageRepository.save(stage);
            auditClient.updateStageTeam(stage);
            log.info("Final active stage: {} for case: {}, completed", stage.getUuid(), caseUUID,
                value(EVENT, STAGE_COMPLETED));
        });

        if (completed) {
            caseData.update(CaseworkConstants.CURRENT_STAGE, "");
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

    CaseSummary getCaseSummary(UUID caseUUID) {
        var caseData = getCaseData(caseUUID);

        var summaryBuilder = new CaseSummary(caseData).withStageDeadlines(
                getStageDeadlines(caseData, caseData.getDataMap())).withPrimaryTopic(caseData.getPrimaryTopic())
            //TODO: HOCS-5558 suspension reimplementation discussion
            .withSuspended(caseData.getDataMap().get("suspended")).withActions(
                caseActionService.getAllCaseActionDataForCase(caseUUID));

        var activeCaseViewData = getActiveCaseData(caseUUID);
        summaryBuilder = summaryBuilder.withPreviousCaseReference(
            activeCaseViewData.getPreviousCaseReference()).withPreviousCaseUUID(
            activeCaseViewData.getPreviousCaseUUID()).withPreviousCaseStageUUID(
            activeCaseViewData.getPreviousCaseStageUUID());

        summaryBuilder.withAdditionalFields(
            caseDataSummaryService.getAdditionalCaseDataFieldsByCaseType(caseData.getType(), caseData.getDataMap()));

        return summaryBuilder;
    }

    private void updateDeadlineForStage(CaseData caseData, String stageType, Integer noOfDays) {
        LocalDate deadline = deadlineService.calculateWorkingDaysForCaseType(caseData.getType(),
            caseData.getDateReceived(), noOfDays);

        caseData.update(String.format("%s_DEADLINE", stageType), deadline.toString());

        if (caseData.getActiveStages()!=null) {
            caseData.getActiveStages().stream().filter(
                stage -> stage.getStageType().equals(stageType)).findFirst().ifPresent(
                activeStage -> activeStage.setDeadline(deadline));
        }
    }

    private Map<String, LocalDate> getStageDeadlines(CaseData caseData, Map<String, String> caseDataMap) {
        Map<String, LocalDate> stageDeadlinesOrig = deadlineService.getAllStageDeadlinesForCaseType(caseData.getType(),
            caseData.getDateReceived());
        // Make a deep copy of the cached map so it isn't modified below
        Map<String, LocalDate> stageDeadlines = new HashMap<>(stageDeadlinesOrig);
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

    Set<GetStandardLineResponse> getStandardLine(UUID caseUUID) {
        CaseData caseData = getCaseData(caseUUID);
        auditClient.viewStandardLineAudit(caseData);
        try {
            GetStandardLineResponse getStandardLineResponse = infoClient.getStandardLine(
                caseData.getPrimaryTopic().getTextUUID());
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
            audit.addAll(
                auditClient.getAuditLinesForCase(caseUUID, caseData.getCreated().toLocalDate(), TIMELINE_EVENTS));
            log.debug("Retrieved {} audit lines", audit.size());
        } catch (Exception e) {
            log.error("Failed to retrieve audit lines for case {}", caseUUID,
                value(EVENT, AUDIT_CLIENT_GET_AUDITS_FOR_CASE_FAILURE), value(EXCEPTION, e));
        }

        Set<CaseNote> notes = caseData.getCaseNotes();

        log.debug("Retrieved {} case notes", notes.size());

        Stream<TimelineItem> auditTimeline = audit.stream().map(
            a -> new TimelineItem(a.getCaseUUID(), a.getStageUUID(), a.getAuditTimestamp().toLocalDateTime(),
                a.getUserID(), a.getType(), a.getAuditPayload(), a.getUuid(), null, null));
        Stream<TimelineItem> notesTimeline = notes.stream().map(n -> {
            String auditPayload = "";
            try {
                auditPayload = objectMapper.writeValueAsString(new AuditPayload.CaseNote(n.getText()));
            } catch (JsonProcessingException e) {
                log.error("Failed to parse case note text for note {}", n.getUuid(), value(EVENT, UNCAUGHT_EXCEPTION),
                    value(EXCEPTION, e));
            }
            return new TimelineItem(n.getCaseUUID(), null, n.getCreated(), n.getAuthor(), n.getCaseNoteType(),
                auditPayload, n.getUuid(), n.getEdited(), n.getEditor());
        });

        return Stream.concat(auditTimeline, notesTimeline);
    }

    public Map<String, String> updateTeamByStageAndTexts(UUID caseUUID,
                                                         UUID stageUUID,
                                                         String stageType,
                                                         String teamUUIDKey,
                                                         String teamNameKey,
                                                         String[] texts) {
        log.debug("Updating Team by Stage: {} {}", stageUUID, stageType);
        Map<String, String> dataMap = getCaseData(caseUUID).getDataMap();
        // build the linkValue text string used to search the team link table by converting "text" key to the case's data value
        StringBuilder linkValue = null;
        for (String text : texts) {
            String value = dataMap.getOrDefault(text, "");
            if (!value.isEmpty()) {
                if (linkValue!=null) {
                    linkValue.append("_");
                    linkValue.append(value);
                } else {
                    linkValue = new StringBuilder(value);
                }
            }
        }

        TeamDto teamDto = infoClient.getTeamByStageAndText(stageType, linkValue.toString());
        Map<String, String> teamMap = new HashMap<>();
        teamMap.put(teamUUIDKey, teamDto.getUuid().toString());
        teamMap.put(teamNameKey, teamDto.getDisplayName());

        return teamMap;
    }

    public Set<UUID> getCaseTeams(UUID caseUUID) {
        log.debug("Retrieving previous teams for : {}", caseUUID);

        Set<GetAuditResponse> auditLines = auditClient.getAuditLinesForCase(caseUUID,
            List.of(STAGE_ALLOCATED_TO_TEAM.toString(), STAGE_CREATED.toString()));
        log.info("Got {} audits", auditLines.size(), value(EVENT, AUDIT_CLIENT_GET_AUDITS_FOR_CASE_SUCCESS));

        return auditLines.stream().map(a -> {
            try {
                return objectMapper.readValue(a.getAuditPayload(),
                    AuditPayload.StageAllocation.class).getAllocatedToUUID();
            } catch (IOException e) {
                log.error("Unable to parse audit payload for reason {}", e.getMessage(),
                    value(EVENT, AUDIT_CLIENT_GET_AUDITS_FOR_CASE_FAILURE), value(EXCEPTION, e));
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public void mapCaseDataValues(UUID caseUUID, Map<String, String> keyMappings) {

        CaseData caseData = caseDataRepository.findActiveByUuid(caseUUID);
        Map<String, String> updatedCaseDataMap = new HashMap<>(caseData.getDataMap());

        if (!updatedCaseDataMap.keySet().containsAll(keyMappings.keySet())) {
            String msg = "Requested keys to map do not exist in case data for caseUUID %s, requested mapping: %s";
            log.error(String.format(msg, caseUUID, keyMappings));
            throw new ApplicationExceptions.DataMappingException(msg, null, DATA_MAPPING_EXCEPTION, caseUUID,
                keyMappings.keySet());
        }

        keyMappings.forEach((String fromKey, String toKey) -> {
            String mappedVal = updatedCaseDataMap.putIfAbsent(toKey, updatedCaseDataMap.get(fromKey));
            if (mappedVal!=null) {
                log.warn(
                    "Requested key to map of key {} to {} cannot take place as key {} already exists and will not be overwritten.",
                    fromKey, toKey, toKey);
            }
        });

        this.updateCaseData(caseData, null, updatedCaseDataMap);
        log.info("Completed mapping of key pairs {} for caseUUID {}", keyMappings, caseUUID,
            value(EVENT, DATA_MAPPING_SUCCESS));
    }

}
