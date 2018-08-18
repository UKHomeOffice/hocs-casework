package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseInputData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseDataRepository;

import javax.transaction.Transactional;
import java.util.UUID;

@Service
@Slf4j
public class CaseDataService {

    private final AuditService auditService;
    private final CaseDataRepository caseDataRepository;
    private final InputDataService inputDataService;


    @Autowired
    public CaseDataService(CaseDataRepository caseDataRepository,
                           InputDataService inputDataService,
                           AuditService auditService) {
        this.caseDataRepository = caseDataRepository;
        this.inputDataService = inputDataService;
        this.auditService = auditService;
    }

    @Transactional
    public CaseData createCase(CaseType caseType) {
        log.debug("Creating Case, Type: {}", caseType);
        CaseData caseData = new CaseData(caseType, caseDataRepository.getNextSeriesId());
        caseDataRepository.save(caseData);
        CaseInputData caseInputData = inputDataService.createInputData(caseData.getUuid());
        caseData.setCaseInputData(caseInputData);
        auditService.writeCreateCaseEvent(caseData);
        log.info("Created Case Type: {} UUID: {}", caseType, caseData.getUuid());
        return caseData;
    }

    @Transactional
    public CaseData getCase(UUID caseUUID) {
        log.debug("Getting Case UUID: {}", caseUUID);
        CaseData caseData = getCaseData(caseUUID);
        CaseInputData caseInputData = inputDataService.getInputData(caseData.getUuid());
        caseData.setCaseInputData(caseInputData);
        auditService.writeGetCaseEvent(caseUUID);
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