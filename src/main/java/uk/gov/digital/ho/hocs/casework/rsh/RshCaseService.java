package uk.gov.digital.ho.hocs.casework.rsh;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseDetails;
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
    private final EmailService emailService;

    @Autowired
    public RshCaseService(CaseService caseService, EmailService emailService) {

        this.caseService = caseService;
        this.emailService = emailService;
    }

    @Transactional
    CaseDetails createRshCase(Map<String, Object> caseData, SendEmailRequest notifyRequest, String username) {
        CaseDetails caseDetails = caseService.createCase("RSH",  username);
        if (caseDetails != null) {
            StageDetails stageDetails = caseService.createStage(caseDetails.getUuid(), "Stage", 0, caseData, username);
            if (stageDetails != null) {
                emailService.sendRshNotify(notifyRequest, caseDetails.getUuid(), username);
                return caseDetails;
            }
        }
        return null;

    }

    CaseDetails updateRshCase(UUID caseUUID, Map<String, Object> caseData, SendEmailRequest notifyRequest, String username) {
        CaseDetails caseDetails = caseService.getCase(caseUUID, username);
        if (caseDetails != null && !caseDetails.getStages().isEmpty()) {
            StageDetails stageDetails = caseDetails.getStages().iterator().next();
            if (stageDetails != null) {
                caseService.updateStage(stageDetails.getUuid(), 0, caseData, username);
                emailService.sendRshNotify(notifyRequest, caseDetails.getUuid(), username);
                return caseDetails;
            }
        }
        return null;
    }

    CaseDetails getRSHCase(UUID caseUUID, String username) {
        return caseService.getCase(caseUUID, username);
    }
}