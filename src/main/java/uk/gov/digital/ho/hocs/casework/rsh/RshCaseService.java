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
import uk.gov.digital.ho.hocs.casework.rsh.dto.SendRshEmailRequest;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.HocsCaseApplication.isNullOrEmpty;

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
    CaseData createRshCase(Map<String, String> caseData, SendRshEmailRequest emailRequest, String username) throws EntityCreationException {
        if (caseData != null) {
            CaseData caseDetails = caseDataService.createCase("RSH", username);
            caseDataService.createStage(caseDetails.getUuid(), "Stage", caseData, username);

            emailService.sendRshEmail(emailRequest, caseDetails.getUuid(), caseDetails.getReference(), caseData.get("outcome"), username);
            return caseDetails;
        } else {
            throw new EntityCreationException("Failed to create case, no caseData!");
        }
    }

    CaseData updateRshCase(UUID caseUUID, Map<String, String> caseData, SendRshEmailRequest emailRequest, String username) throws EntityCreationException, EntityNotFoundException {
        if (!isNullOrEmpty(caseUUID) && caseData != null) {
            CaseData caseDetails = caseDataService.getCase(caseUUID, username);
            if (!caseDetails.getStages().isEmpty()) {
                StageData stageData = caseDetails.getStages().iterator().next();
                caseDataService.updateStage(caseUUID, stageData.getUuid(), "Stage", caseData, username);
                emailService.sendRshEmail(emailRequest, caseDetails.getUuid(), caseDetails.getReference(), caseData.get("outcome"), username);
                return caseDetails;
            } else {
                throw new EntityCreationException("Failed to update case, case has no stages!");
            }
        } else {
            throw new EntityCreationException("Failed to update case, no caseUUID or caseData!");
        }
    }

    CaseData getRSHCase(UUID caseUUID, String username) throws EntityNotFoundException {
        if (!isNullOrEmpty(caseUUID)) {
            CaseData caseData = caseDataService.getCase(caseUUID, username);
            if (caseData != null) {
                return caseData;
            } else {
                throw new EntityNotFoundException("Case not found!");
            }
        } else {
            throw new EntityNotFoundException("Failed to get case, no caseUUID!");
        }
    }
}