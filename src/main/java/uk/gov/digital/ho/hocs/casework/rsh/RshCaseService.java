package uk.gov.digital.ho.hocs.casework.rsh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.audit.AuditAction;
import uk.gov.digital.ho.hocs.casework.audit.AuditEntry;
import uk.gov.digital.ho.hocs.casework.audit.AuditRepository;
import uk.gov.digital.ho.hocs.casework.caseDetails.*;
import uk.gov.digital.ho.hocs.casework.notify.NotifyRequest;
import uk.gov.digital.ho.hocs.casework.notify.NotifyService;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class RshCaseService {

    private final NotifyService notifyService;

    private final CaseService caseService;
    private final AuditRepository auditRepository;
    private final CaseDetailsRepository caseDetailsRepository;

    @Autowired
    public RshCaseService(NotifyService notifyService, CaseService caseService, CaseDetailsRepository caseDetailsRepository, StageDetailsRepository stageDetailsRepository, AuditRepository auditRepository) {

        this.notifyService = notifyService;
        this.caseService = caseService;
        this.caseDetailsRepository = caseDetailsRepository;
        this.auditRepository = auditRepository;
    }

    public CaseDetails createRshCase(Map<String, Object> caseData, NotifyRequest notifyRequest, String username) {
        CaseDetails caseDetails = caseService.createCase("RSH",  username);
        caseService.createStage(caseDetails.getUuid(),"OnlyStage", 0, caseData, username);

        if(caseDetails.getId() != 0) {
            notifyService.sendRshNotify(notifyRequest, caseDetails.getUuid());
        }
        return caseDetails;
    }

    public CaseDetails updateRshCase(UUID caseUUID, Map<String, Object> caseData, NotifyRequest notifyRequest, String username) {
        CaseDetails caseDetails = getRSHCase(caseUUID, username);
        if(!caseDetails.getStages().isEmpty()) {
            StageDetails stageDetails = caseDetails.getStages().iterator().next();
            caseService.updateStage(stageDetails.getUuid(),0,caseData, username);
        }

        if(caseDetails.getId() != 0) {
            notifyService.sendRshNotify(notifyRequest, caseDetails.getUuid());
        }
        return caseDetails;
    }

    @Transactional
    public CaseDetails getRSHCase(UUID uuid, String username) {
        log.info("Requesting Case, UUID: {}, User: {}", uuid, username);
        CaseDetails caseDetails = caseDetailsRepository.findByUuid(uuid);
        AuditEntry auditEntry = new AuditEntry(username, uuid.toString(), AuditAction.GET_CASE);
        auditRepository.save(auditEntry);
        log.info("Found Case, Reference: {} ({}), User: {}", caseDetails.getReference(), caseDetails.getUuid(), auditEntry.getUsername());
        return caseDetails;
    }

}