package uk.gov.digital.ho.hocs.casework.casedetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.InputData;
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
    public void setInputData(UUID caseUUID, Map<String, String> data) {
        log.debug("Setting Input Data for Case UUID: {}", caseUUID);
        InputData inputData = inputDataRepository.findByCaseUUID(caseUUID);
        if (inputData == null) {
            createInputData(caseUUID, data);
        } else {
            updateInputData(inputData, data);
        }
        log.info("Set Input Data for Case UUID: {}", caseUUID);
    }

    private void createInputData(UUID caseUUID, Map<String, String> data) {
        log.debug("Creating new Input Data for Case UUID: {}", caseUUID);
        InputData inputData = new InputData(caseUUID);
        inputData.updateData(data, objectMapper);
        inputDataRepository.save(inputData);
        auditService.createInputDataEvent(inputData);
    }

    private void updateInputData(InputData inputData, Map<String, String> data) {
        log.debug("Updating Input Data for Case UUID: {}", inputData.getCaseUUID());
        inputData.updateData(data, objectMapper);
        inputDataRepository.save(inputData);
        auditService.updateInputDataEvent(inputData);
    }

    InputData getInputData(UUID caseUUID) {
        log.debug("Getting Input Data for Case UUID: {}", caseUUID);
        InputData inputData = inputDataRepository.findByCaseUUID(caseUUID);
        if (inputData != null) {
            log.info("Got Input Data for Case UUID: {}", caseUUID);
            return inputData;
        } else {
            throw new EntityNotFoundException("InputData UUID: %s not found!", caseUUID);
        }
    }
}