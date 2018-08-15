package uk.gov.digital.ho.hocs.casework.casedetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseInputData;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseInputDataRepository;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class InputDataService {

    private final AuditService auditService;
    private final CaseInputDataRepository caseInputDataRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public InputDataService(CaseInputDataRepository caseInputDataRepository,
                            AuditService auditService,
                            ObjectMapper objectMapper) {
        this.auditService = auditService;
        this.caseInputDataRepository = caseInputDataRepository;
        this.objectMapper = objectMapper;
    }


    @Transactional
    public void updateStage(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        log.debug("Updating Stage UUID: {}", stageUUID);
        CaseInputData caseInputData = caseInputDataRepository.findByCaseUUID(caseUUID);
        caseInputData.updateData(data, objectMapper);
        caseInputDataRepository.save(caseInputData);
        // TODO: this is wrong. - doesn't capture data anymore
        auditService.writeUpdateStageEvent(stageUUID, caseInputData);
        log.info("Updated Stage UUID: {}", stageUUID);
    }
}