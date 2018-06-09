package uk.gov.digital.ho.hocs.casework.rsh;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseDetails;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseDetailsRepository;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseService;
import uk.gov.digital.ho.hocs.casework.caseDetails.StageDetails;
import uk.gov.digital.ho.hocs.casework.email.EmailService;
import uk.gov.digital.ho.hocs.casework.email.SendEmailRequest;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class RshCaseService {

    private final CaseService caseService;
    private final AuditService auditService;
    private final CaseDetailsRepository caseDetailsRepository;

    @Autowired
    public RshCaseService(CaseService caseService, CaseDetailsRepository caseDetailsRepository, AuditService auditService) {
        this.caseService = caseService;
        this.caseDetailsRepository = caseDetailsRepository;
        this.auditService = auditService;
    }

    public CaseDetails createRshCase(Map<String, Object> caseData, SendEmailRequest notifyRequest, String username) {
        CaseDetails caseDetails = caseService.createCase("RSH",  username);
        caseService.createStage(caseDetails.getUuid(),"OnlyStage", 0, caseData, username);

        if(caseDetails.getId() != 0) {
            EmailService.sendRshNotify(notifyRequest, caseDetails.getUuid(), username);
        }
        return caseDetails;
    }

    public CaseDetails updateRshCase(UUID caseUUID, Map<String, Object> caseData, SendEmailRequest notifyRequest, String username) {
        CaseDetails caseDetails = getRSHCase(caseUUID, username);
        if(!caseDetails.getStages().isEmpty()) {
            StageDetails stageDetails = caseDetails.getStages().iterator().next();
            caseService.updateStage(stageDetails.getUuid(),0,caseData, username);
        }

        if(caseDetails.getId() != 0) {
            EmailService.sendRshNotify(notifyRequest, caseDetails.getUuid(), username);
        }
        return caseDetails;
    }

    @Transactional
    public CaseDetails getRSHCase(UUID uuid, String username) {
        auditService.writeGetCaseEvent(username, uuid);
        log.info("Requesting Case, UUID: {}, User: {}", uuid, username);
        CaseDetails caseDetails = caseDetailsRepository.findByUuid(uuid);
        log.info("Found Case, Reference: {} ({}), User: {}", caseDetails.getReference(), caseDetails.getUuid(), username);
        return caseDetails;
    }

}