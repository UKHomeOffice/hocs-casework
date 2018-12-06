package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.util.Map;
import java.util.UUID;

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

    public CaseData createCase(CaseDataType caseType, Map<String, String> data) {
        Long caseNumber = caseDataRepository.getNextSeriesId();
        CaseData caseData = new CaseData(caseType, caseNumber, data, objectMapper);
        caseDataRepository.save(caseData);
        log.info("Created Case Type: {} UUID: {}", caseType.getDisplayCode(), caseData.getUuid());
        return caseData;
    }

    public CaseData getCase(UUID caseUUID) {
        CaseData caseData = caseDataRepository.findByUuid(caseUUID);
        if (caseData != null) {
            log.info("Got Case: {}", caseData.getUuid());
            return caseData;
        } else {
            throw new EntityNotFoundException("Case: %s, not found!", caseUUID);
        }
    }

    public void updateCaseData(UUID caseUUID, Map<String, String> data) {
        if (data != null) {
            CaseData caseData = getCase(caseUUID);
            caseData.update(data, objectMapper);
            caseDataRepository.save(caseData);
            log.info("Updated Case Data for Case: {}", caseUUID);
        }
    }

    public void updatePriority(UUID caseUUID, boolean priority) {
        CaseData caseData = getCase(caseUUID);
        caseData.setPriority(priority);
        caseDataRepository.save(caseData);
        log.info("Updated Case Data for Case: {}", caseUUID);
    }

    public void deleteCase(UUID caseUUID) {
        caseDataRepository.deleteCase(caseUUID);
        log.info("Deleted Case: {}", caseUUID);

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
}
