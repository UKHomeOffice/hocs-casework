package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
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
    public CaseData createCase(CaseType caseType) {
        log.info("Creating Case, Type: {}", caseType);
        CaseData caseData = new CaseData(caseType, caseDataRepository.getNextSeriesId());
        caseDataRepository.save(caseData);
        auditService.writeCreateCaseEvent(caseData);
        log.info("Created Case UUID: {} ({})", caseData.getUuid(), caseData.getReference());
        return caseData;
    }

    @Transactional
    public CaseData getCase(UUID uuid) {
        log.debug("Getting Case UUID: {}", uuid);
        CaseData caseData = caseDataRepository.findByUuid(uuid);
        auditService.writeGetCaseEvent(uuid);
        if (caseData != null) {
            log.info("Got Case UUID: {} ({})", caseData.getUuid(), caseData.getReference());
            return caseData;
        } else {
            throw new EntityNotFoundException("Case UUID: %s,  not found!", caseData.getUuid());
        }
    }

    @Transactional
    public void updateCase(UUID caseUUID) {
        log.info("Updating Case UUID: {}", caseUUID);
        CaseData caseData = caseDataRepository.findByUuid(caseUUID);
        if (caseData != null) {
            caseDataRepository.save(caseData);
            auditService.writeUpdateCaseEvent(caseData);
            log.info("Updated Case UUID: {} ({}),", caseData.getUuid(), caseData.getReference());
        } else {
            throw new EntityNotFoundException("Case not found!");
        }
    }
}