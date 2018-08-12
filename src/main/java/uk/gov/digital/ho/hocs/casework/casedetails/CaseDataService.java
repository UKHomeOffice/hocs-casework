package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseInputDataRepository;

import javax.transaction.Transactional;
import java.util.UUID;

@Service
@Slf4j
public class CaseDataService {

    private final AuditService auditService;
    private final CaseDataRepository caseDataRepository;
    private final CaseInputDataRepository caseInputDataRepository;


    @Autowired
    public CaseDataService(CaseDataRepository caseDataRepository,
                           CaseInputDataRepository caseInputDataRepository,
                           AuditService auditService) {
        this.caseDataRepository = caseDataRepository;
        this.caseInputDataRepository = caseInputDataRepository;
        this.auditService = auditService;
    }

    @Transactional
    public CaseData createCase(CaseType caseType) {
        log.info("Creating Case, Type: {}", caseType);
        CaseData caseData = new CaseData(caseType, caseDataRepository.getNextSeriesId());
        caseDataRepository.save(caseData);
        caseInputDataRepository.save(caseData.getCaseInputData());
        auditService.writeCreateCaseEvent(caseData);
        log.info("Created Case Type: {} UUID: {}", caseType, caseData.getUuid());
        return caseData;
    }

    @Transactional
    public CaseData getCase(UUID caseUUID) {
        log.debug("Getting Case UUID: {}", caseUUID);
        CaseData caseData = caseDataRepository.findByUuid(caseUUID);
        auditService.writeGetCaseEvent(caseUUID);
        if (caseData != null) {
            caseData.setCaseInputData(caseInputDataRepository.findByCaseUUID(caseData.getUuid()));

            log.info("Got Case UUID: {}", caseData.getUuid());
            return caseData;
        } else {
            throw new EntityNotFoundException("Case UUID: %s, not found!", caseUUID);
        }
    }

    /* We don't currently allow updating a case */
    //@Transactional
    //public void updateCase(UUID caseUUID) {
    //    log.debug("Updating Case UUID: {}", caseUUID);
    //    CaseData caseData = caseDataRepository.findByUuid(caseUUID);
    //    if (caseData != null) {
    //        caseDataRepository.save(caseData);
    //        auditService.writeUpdateCaseEvent(caseData);
    //        log.info("Updated Case UUID: {},", caseData.getUuid());
    //    } else {
    //        throw new EntityNotFoundException("Case UUID: %s, not found!", caseUUID);
    //    }
    //}
}