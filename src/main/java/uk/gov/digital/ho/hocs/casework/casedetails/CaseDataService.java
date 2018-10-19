package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseDataRepository;

import javax.transaction.Transactional;
import java.util.UUID;

@Service
@Slf4j
public class CaseDataService {

    private final AuditService auditService;
    private final CaseDataRepository caseDataRepository;

    @Autowired
    public CaseDataService(CaseDataRepository caseDataRepository,
                           AuditService auditService) {
        this.caseDataRepository = caseDataRepository;
        this.auditService = auditService;
    }

    @Transactional
    public CaseData createCase(CaseDataType caseDataType) {
        log.debug("Creating Case, Type: {}", caseDataType);
        CaseData caseData = new CaseData(caseDataType, caseDataRepository.getNextSeriesId());
        caseDataRepository.save(caseData);
        auditService.createCaseEvent(caseData);
        log.info("Created Case Type: {} UUID: {}", caseDataType, caseData.getUuid());
        return caseData;
    }

    public CaseData getCase(UUID caseUUID) {
        log.debug("Getting Case UUID: {}", caseUUID);
        CaseData caseData = caseDataRepository.findByUuid(caseUUID);
        if (caseData != null) {
            log.info("Got Case UUID: {}", caseData.getUuid());
            return caseData;
        } else {
            throw new EntityNotFoundException("Case UUID: %s, not found!", caseUUID);
        }
    }

    public void deleteCase(UUID caseUUID) {
        caseDataRepository.delete(caseUUID);
        log.info("Deleted Case: {}", caseUUID);


    }
}