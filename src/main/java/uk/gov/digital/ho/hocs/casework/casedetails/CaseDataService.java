package uk.gov.digital.ho.hocs.casework.casedetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseDataRepository;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class CaseDataService {

    private final CaseDataRepository caseDataRepository;
    private final ObjectMapper objectMapper;


    @Autowired
    public CaseDataService(CaseDataRepository caseDataRepository, ObjectMapper objectMapper) {
        this.caseDataRepository = caseDataRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CaseData createCase(CaseDataType caseDataType) {
        CaseData caseData = new CaseData(caseDataType, caseDataRepository.getNextSeriesId());
        caseDataRepository.save(caseData);
        log.info("Created Case Type: {} UUID: {}", caseDataType, caseData.getUuid());
        return caseData;
    }

    @Transactional
    public CaseData getCase(UUID caseUUID) {
        CaseData caseData = caseDataRepository.findByUuid(caseUUID);
        if (caseData != null) {
            log.info("Got Case: {}", caseData.getUuid());
            return caseData;
        } else {
            throw new EntityNotFoundException("Case: %s, not found!", caseUUID);
        }
    }

    @Transactional
    public void updateCaseData(UUID caseUUID, Map<String, String> data) {
        CaseData caseData = getCase(caseUUID);
        caseData.updateData(data, objectMapper);
        caseDataRepository.save(caseData);
        log.info("Updated Case Data for Case: {}", caseUUID);

    }

    @Transactional
    public void deleteCase(UUID caseUUID) {
        caseDataRepository.delete(caseUUID);
        log.info("Deleted Case: {}", caseUUID);


    }
}