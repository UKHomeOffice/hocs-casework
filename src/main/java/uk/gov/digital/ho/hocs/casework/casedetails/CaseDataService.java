package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.casedetails.model.InputData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseDataRepository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class CaseDataService {

    private final AuditService auditService;
    private final CaseDataRepository caseDataRepository;
    private final InputDataService inputDataService;
    private final StageDataService stageDataService;


    @Autowired
    public CaseDataService(CaseDataRepository caseDataRepository,
                           InputDataService inputDataService,
                           AuditService auditService,
                           StageDataService stageDataService) {
        this.caseDataRepository = caseDataRepository;
        this.inputDataService = inputDataService;
        this.stageDataService = stageDataService;
        this.auditService = auditService;
    }

    @Transactional
    public CaseData createCase(CaseType caseType, LocalDate dateReceived) {
        log.debug("Creating Case, Type: {}", caseType);
        CaseData caseData = new CaseData(caseType, caseDataRepository.getNextSeriesId());
        caseDataRepository.save(caseData);
        // TODO: this should be passed in as a map from UI down.
        Map<String, String> data = new HashMap<>();
        data.put("DateReceived", dateReceived.toString());
        InputData inputData = inputDataService.createInputData(caseData.getUuid(), data);
        caseData.setInputData(inputData);
        auditService.createCaseEvent(caseData);
        log.info("Created Case Type: {} UUID: {}", caseType, caseData.getUuid());
        return caseData;
    }

    @Transactional
    public CaseData getCase(UUID caseUUID) {
        log.debug("Getting Case UUID: {}", caseUUID);
        CaseData caseData = getCaseData(caseUUID);
        Set<StageData> stageData = stageDataService.getStagesForCase(caseUUID);
        caseData.setStages(stageData);
        InputData inputData = inputDataService.getInputData(caseData.getUuid());
        caseData.setInputData(inputData);
        auditService.getCaseEvent(caseUUID);
        log.info("Got Case UUID: {}", caseData.getUuid());
        return caseData;
    }

    private CaseData getCaseData(UUID caseUUID) {
        CaseData caseData = caseDataRepository.findByUuid(caseUUID);
        if (caseData != null) {
            return caseData;
        } else {
            throw new EntityNotFoundException("Case UUID: %s, not found!", caseUUID);
        }
    }
}