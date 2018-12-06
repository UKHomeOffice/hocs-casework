package uk.gov.digital.ho.hocs.casework.api;

import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.ActiveStage;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseSummary;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import javax.transaction.Transactional;
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
    private final ObjectMapper objectMapper;
    private final InfoClient infoClient;
    private final CorrespondentService correspondentService;
    private final StageService stageService;

    @Autowired
    public CaseDataService(CaseDataRepository caseDataRepository, InfoClient infoClient,
                           ObjectMapper objectMapper, CorrespondentService correspondentService,
                            StageService stageService) {
        this.caseDataRepository = caseDataRepository;
        this.infoClient = infoClient;
        this.objectMapper = objectMapper;
        this.correspondentService = correspondentService;
        this.stageService = stageService;
    }

    @Transactional
    public CaseData createCase(CaseDataType caseType, Map<String, String> data, LocalDate caseDeadline, LocalDate dateReceived) {
        Long caseNumber = caseDataRepository.getNextSeriesId();
        CaseData caseData = new CaseData(caseType, caseNumber, data, objectMapper, caseDeadline, dateReceived);

        caseDataRepository.save(caseData);
        log.info("Created Case Type: {} UUID: {}", caseType.getDisplayCode(), caseData.getUuid(), value(EVENT, CASE_CREATED));
        return caseData;
    }

    @Transactional
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

    @Transactional
    public void updateCaseData(UUID caseUUID, Map<String, String> data) {
        if (data != null) {
            CaseData caseData = getCase(caseUUID);
            caseData.update(data, objectMapper);
            caseDataRepository.save(caseData);
            log.info("Updated Case Data for Case: {}", caseUUID, value(EVENT, CASE_UPDATED));
        }
    }

    @Transactional
    public void updatePriority(UUID caseUUID, boolean priority) {
        CaseData caseData = getCase(caseUUID);
        caseData.setPriority(priority);
        caseDataRepository.save(caseData);
        log.info("Updated Case Data for Case: {}", caseUUID, value(EVENT, PRIORITY_UPDATED));
    }

    @Transactional
    public void deleteCase(UUID caseUUID) {
        caseDataRepository.deleteCase(caseUUID);
        log.info("Deleted Case: {}", caseUUID, value(EVENT, CASE_DELETED));

    }

    public CaseDataType getCaseTypeByUUID(UUID uuid) {
        String shortCode = uuid.toString().substring(34);
        return infoClient.getCaseTypeByShortCode(shortCode);
    }

    public CaseSummary getCaseSummary(UUID caseUUID) throws IOException {
        CaseData caseData = caseDataRepository.findByUuid(caseUUID);
        if (caseData == null) {
            log.error("Case: {}, not found!", caseUUID, value(EVENT, CASE_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case: %s, not found!", caseUUID), CASE_NOT_FOUND);
        }

        Map<String, String> additionalData = new HashMap<>();
        Set<String> fieldSchema = infoClient.getCaseSummaryFields(caseData.getType());
        Map<StageType, LocalDate> stageDeadlines = infoClient.getDeadlines(caseData.getType(), caseData.getDateReceived());

        if(!StringUtils.isNullOrEmpty(caseData.getData())) {
            Map<String, String> jsonData = objectMapper.readValue(caseData.getData(), new TypeReference<Map<String, Object>>() {
            });
            additionalData.putAll(jsonData.entrySet().stream()
                    .filter(d -> fieldSchema.contains(d.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
        Correspondent primaryCorrespondent = correspondentService.getCorrespondent(caseData.getUuid(), caseData.getPrimaryCorrespondentUUID());
        Set<ActiveStage> activeStages = stageService.getActiveStagesByCaseUUID(caseUUID).stream().map(stage -> ActiveStage.from(stage)).collect(Collectors.toSet());
        return new CaseSummary(caseData.getCaseDeadline(), stageDeadlines, additionalData,primaryCorrespondent, activeStages);

    }
}
