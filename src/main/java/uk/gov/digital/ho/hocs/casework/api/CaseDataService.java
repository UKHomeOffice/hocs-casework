package uk.gov.digital.ho.hocs.casework.api;

import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.ActiveStage;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseSummary;
import uk.gov.digital.ho.hocs.casework.api.dto.CorrespondentDto;
import uk.gov.digital.ho.hocs.casework.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public CaseData createCase(CaseDataType caseType, Map<String, String> data, LocalDate caseDeadline, LocalDate dateReceived) {
        Long caseNumber = caseDataRepository.getNextSeriesId();
        CaseData caseData = new CaseData(caseType, caseNumber, data, objectMapper, caseDeadline, dateReceived);
        caseDataRepository.save(caseData);
        auditClient.createCaseAudit(caseData);
        log.info("Created Case Type: {} UUID: {}", caseType.getDisplayCode(), caseData.getUuid(), value(EVENT, CASE_CREATED));
        return caseData;
    }

    public CaseData getCase(UUID caseUUID) {
        CaseData caseData = caseDataRepository.findByUuid(caseUUID);
        if (caseData != null) {
            log.info("Got Case: {}", caseData.getUuid(), value(EVENT, CASE_RETRIEVED));
            return caseData;
        } else {
            log.error("Case: {}, not found!", caseUUID, value(EVENT, CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case: %s, not found!", caseUUID), CASE_NOT_FOUND);
        }
    }

    public void updateCaseData(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        if (data != null) {
            CaseData caseData = getCase(caseUUID);
            caseData.update(data, objectMapper);
            caseDataRepository.save(caseData);
            log.info("Updated Case Data for Case: {}", caseUUID, value(EVENT, CASE_UPDATED));
        }
    }

    public void updatePriority(UUID caseUUID, boolean priority) {
        CaseData caseData = getCase(caseUUID);
        caseData.setPriority(priority);
        caseDataRepository.save(caseData);
        log.info("Updated Case Data for Case: {}", caseUUID, value(EVENT, PRIORITY_UPDATED));
    }

    public void deleteCase(UUID caseUUID) {
        caseDataRepository.deleteCase(caseUUID);
        log.info("Deleted Case: {}", caseUUID, value(EVENT, CASE_DELETED));

    }

    public String getCaseType(UUID caseUUID) {
        CaseDataType caseDataType = infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34));
        if (caseDataType == null) {
            log.warn("Cannot determine type of caseUUID {} falling back to database lookup", caseUUID);
            return getCase(caseUUID).getType();
        } else {
            return caseDataType.getDisplayCode();
        }
    }

    public CaseSummary getCaseSummary(UUID caseUUID) throws IOException {
        CaseData caseData = caseDataRepository.findByUuid(caseUUID);
        if (caseData == null) {
            log.error("Case: {}, not found!", caseUUID, value(EVENT, CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case: %s, not found!", caseUUID), CASE_NOT_FOUND);
        }

        Map<String, String> additionalData = new HashMap<>();
        Set<String> fieldSchema = infoClient.getCaseSummaryFields(caseData.getType());
        Map<String, LocalDate> stageDeadlines = infoClient.getDeadlines(caseData.getType(), caseData.getDateReceived());

        if(!StringUtils.isNullOrEmpty(caseData.getData())) {
            Map<String, String> jsonData = objectMapper.readValue(caseData.getData(), new TypeReference<Map<String, Object>>() {
            });
            additionalData.putAll(jsonData.entrySet().stream()
                    .filter(d -> fieldSchema.contains(d.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
        CorrespondentDto primaryCorrespondent = null;
        if (caseData.getPrimaryCorrespondentUUID() != null) {
            primaryCorrespondent = CorrespondentDto.from(correspondentService.getCorrespondent(caseData.getUuid(), caseData.getPrimaryCorrespondentUUID()));
        }
        Set<ActiveStage> activeStages = stageService.getActiveStagesByCaseUUID(caseUUID).stream().map(stage -> ActiveStage.from(stage)).collect(Collectors.toSet());
        return new CaseSummary(caseData.getCaseDeadline(), stageDeadlines, additionalData,primaryCorrespondent, activeStages);

    }
}
