package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseDataRepository;

import javax.transaction.Transactional;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.HocsCaseApplication.isNullOrEmpty;

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
        log.info("Requesting Create Case, Type: {}", caseType);
        if (!isNullOrEmpty(caseType)) {
            CaseData caseData = new CaseData(caseType.toString(), caseDataRepository.getNextSeriesId());
            caseDataRepository.save(caseData);
            auditService.writeCreateCaseEvent(caseData);
            log.info("Created Case, Reference: {}, UUID: {}", caseData.getReference(), caseData.getUuid());
            return caseData;
        } else {
            throw new EntityCreationException("Failed to create case, invalid caseType!");
        }
    }

    @Transactional
    public void updateCase(UUID caseUUID) {
        log.info("Requesting Update Case: {}", caseUUID);
        if (!isNullOrEmpty(caseUUID)) {
            CaseData caseData = caseDataRepository.findByUuid(caseUUID);
            if (caseData != null) {
                caseDataRepository.save(caseData);
                auditService.writeUpdateCaseEvent(caseData);
                log.info("Updated Case, Reference: {}, UUID: {}", caseData.getReference(), caseData.getUuid());
            } else {
                throw new EntityNotFoundException("Case not found!");
            }
        } else {
            throw new EntityCreationException("Failed to create case, invalid caseType!");
        }
    }

    @Transactional
    public CaseData getCase(UUID uuid) {
        log.info("Requesting Case, UUID: {}", uuid);
        if (uuid != null) {
            CaseData caseData = caseDataRepository.findByUuid(uuid);
            auditService.writeGetCaseEvent( uuid);
            if (caseData != null) {
                log.info("Found Case, Reference: {} ({})", caseData.getReference(), caseData.getUuid());
                return caseData;
            } else {
                throw new EntityNotFoundException("Case not Found!");
            }
        } else {
            throw new EntityNotFoundException("CaseUUID was null!");
        }
    }
}