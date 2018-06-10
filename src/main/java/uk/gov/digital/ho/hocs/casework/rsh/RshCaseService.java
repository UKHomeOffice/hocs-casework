package uk.gov.digital.ho.hocs.casework.rsh;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseDataService;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.email.EmailService;
import uk.gov.digital.ho.hocs.casework.email.dto.SendEmailRequest;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class RshCaseService {

    private final CaseDataService caseDataService;
    private final EmailService emailService;

    @Autowired
    public RshCaseService(CaseDataService caseDataService, EmailService emailService) {
        this.caseDataService = caseDataService;
        this.emailService = emailService;
    }

    @Transactional
    CaseData createRshCase(Map<String, Object> caseData, SendEmailRequest notifyRequest, String username) {
        CaseData caseDetails = caseDataService.createCase("RSH", username);
        if (caseDetails != null) {
            StageData stageData = caseDataService.createStage(caseDetails.getUuid(), "Stage", 0, caseData, username);
            if (stageData != null) {
                emailService.sendRshNotify(notifyRequest, caseDetails.getUuid(), username);
                return caseDetails;
            }
        }
        return null;

    }

    CaseData updateRshCase(UUID caseUUID, Map<String, Object> caseData, SendEmailRequest notifyRequest, String username) {
        CaseData caseDetails = caseDataService.getCase(caseUUID, username);
        if (caseDetails != null && !caseDetails.getStages().isEmpty()) {
            StageData stageData = caseDetails.getStages().iterator().next();
            if (stageData != null) {
                caseDataService.updateStage(stageData.getUuid(), 0, caseData, username);
                emailService.sendRshNotify(notifyRequest, caseDetails.getUuid(), username);
                return caseDetails;
            }
        }
        return null;
    }

    CaseData getRSHCase(UUID caseUUID, String username) {
        return caseDataService.getCase(caseUUID, username);
    }
}