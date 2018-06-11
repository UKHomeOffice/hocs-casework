package uk.gov.digital.ho.hocs.casework.rsh;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseDataService;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityNotFoundException;
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
    CaseData createRshCase(Map<String, String> caseData, SendEmailRequest notifyRequest, String username) throws EntityCreationException {
        CaseData caseDetails = caseDataService.createCase("RSH", username);
        StageData stageData = caseDataService.createStage(caseDetails.getUuid(), "Stage", caseData, username);
        emailService.sendRshNotify(notifyRequest, caseDetails.getUuid(), username);
        return caseDetails;
    }

    CaseData updateRshCase(UUID caseUUID, Map<String, String> caseData, SendEmailRequest notifyRequest, String username) throws EntityCreationException, EntityNotFoundException {
        CaseData caseDetails = caseDataService.getCase(caseUUID, username);
        StageData stageData = caseDetails.getStages().iterator().next();
        caseDataService.updateStage(caseUUID, stageData.getUuid(), "Stage", caseData, username);
        emailService.sendRshNotify(notifyRequest, caseDetails.getUuid(), username);
        return caseDetails;
    }

    CaseData getRSHCase(UUID caseUUID, String username) throws EntityNotFoundException {
        return caseDataService.getCase(caseUUID, username);
    }
}