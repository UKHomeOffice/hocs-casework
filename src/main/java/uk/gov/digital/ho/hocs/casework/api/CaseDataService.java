package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Service
@Slf4j
public class CaseDataService {

    private final CaseDataRepository caseDataRepository;
    private final ObjectMapper objectMapper;
    private final InfoClient infoClient;

    @Autowired
    public CaseDataService(CaseDataRepository caseDataRepository, InfoClient infoClient, ObjectMapper objectMapper) {
        this.caseDataRepository = caseDataRepository;
        this.infoClient = infoClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CaseData createCase(CaseDataType caseType, Map<String, String> data, LocalDate caseDeadline) {
        Long caseNumber = caseDataRepository.getNextSeriesId();
        CaseData caseData = new CaseData(caseType, caseNumber, data, objectMapper, caseDeadline);

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
}
