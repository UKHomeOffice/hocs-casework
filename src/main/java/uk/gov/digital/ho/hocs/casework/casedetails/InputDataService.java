package uk.gov.digital.ho.hocs.casework.casedetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseInputData;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.InputDataRepository;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class InputDataService {

    private final AuditService auditService;
    private final InputDataRepository inputDataRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public InputDataService(InputDataRepository inputDataRepository,
                            AuditService auditService,
                            ObjectMapper objectMapper) {
        this.auditService = auditService;
        this.inputDataRepository = inputDataRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CaseInputData createInputData(UUID caseUUID) {
        log.debug("Updating Input Data for Case UUID: {}", caseUUID);
        CaseInputData caseInputData = new CaseInputData(caseUUID);
        inputDataRepository.save(caseInputData);
        auditService.writeCreateInputDataEvent(caseInputData);
        log.info("Updated Input Data for Case UUID: {}", caseUUID);
        return caseInputData;
    }

    @Transactional
    public void updateInputData(UUID caseUUID, Map<String, String> data) {
        log.debug("Updating Input Data for Case UUID: {}", caseUUID);
        CaseInputData caseInputData = getInputData(caseUUID);
        caseInputData.updateData(data, objectMapper);
        inputDataRepository.save(caseInputData);
        auditService.writeUpdateInputDataEvent(caseInputData);
        log.info("Updated Input Data for Case UUID: {}", caseUUID);
    }

    CaseInputData getInputData(UUID caseUUID) {
        log.debug("Getting Input Data for Case UUID: {}", caseUUID);
        CaseInputData caseInputData = inputDataRepository.findByCaseUUID(caseUUID);
        if (caseInputData != null) {
            log.info("Got Input Data for Case UUID: {}", caseUUID);
            return caseInputData;
        } else {
            throw new EntityNotFoundException("InputData UUID: %s not found!", caseUUID);
        }
    }
}