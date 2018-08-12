package uk.gov.digital.ho.hocs.casework.rsh;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.CaseDataService;
import uk.gov.digital.ho.hocs.casework.casedetails.StageDataService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;
import uk.gov.digital.ho.hocs.casework.rsh.dto.SendRshEmailRequest;
import uk.gov.digital.ho.hocs.casework.rsh.email.EmailService;
import uk.gov.digital.ho.hocs.casework.rsh.email.dto.SendEmailRequest;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class RshCaseService {

    private final CaseDataService caseDataService;
    private final StageDataService stageDataService;
    private final EmailService emailService;

    private final String frontendUrl;

    @Autowired
    public RshCaseService(CaseDataService caseDataService,
                          StageDataService stageDataService,
                          EmailService emailService,
                          @Value("${notify.frontend.url}") String frontEndUrl) {
        this.caseDataService = caseDataService;
        this.stageDataService = stageDataService;
        this.emailService = emailService;
        this.frontendUrl = frontEndUrl;
    }

    private static SendEmailRequest createRshEmail(String emailAddress, String teamName, String frontEndUrl, UUID caseUUID, String caseReference, String caseStatus) {
        Map<String, String> personalisation = new HashMap<>();
        personalisation.put("team", teamName);
        personalisation.put("link", frontEndUrl + "/case/" + caseUUID);
        personalisation.put("reference", caseReference);
        personalisation.put("caseStatus", caseStatus);

        return new SendEmailRequest(emailAddress, personalisation);
    }

    CaseData getRSHCase(UUID caseUUID) {
        CaseData caseData = caseDataService.getCase(caseUUID);
        if (caseData != null) {
            return caseData;
        } else {
            throw new EntityNotFoundException("Case not found!");
        }
    }

    @Transactional
    public CaseData createRshCase(Map<String, String> caseData, SendRshEmailRequest emailRequest) {
        CaseData caseDetails = caseDataService.createCase(CaseType.RSH);
        if (caseDetails != null) {
            stageDataService.createStage(caseDetails.getUuid(), StageType.RUSH_ONLY_STAGE, null, null);
            sendRshEmail(emailRequest, caseDetails.getUuid(), caseDetails.getCaseInputData().getReference(), caseData.get("outcome"));
            return caseDetails;
        } else {
            throw new EntityCreationException("Failed to create case, no casedetails!");
        }
    }

    @Transactional
    public CaseData updateRshCase(UUID caseUUID, Map<String, String> caseData, SendRshEmailRequest emailRequest) {
        CaseData caseDetails = caseDataService.getCase(caseUUID);
        if (!caseDetails.getStages().isEmpty()) {
            StageData stageData = caseDetails.getStages().iterator().next();
            //stageDataService.updateStage(caseUUID, stageData.getUuid(), caseData);
            sendRshEmail(emailRequest, caseDetails.getUuid(), caseDetails.getCaseInputData().getReference(), caseData.get("outcome"));
            return caseDetails;
        } else {
            throw new EntityCreationException("Failed to update case, case has no stages!");
        }
    }

    private void sendRshEmail(SendRshEmailRequest emailRequest, UUID caseUUID, String caseReference, String caseStatus) {
        if (emailRequest != null) {
            SendEmailRequest sendEmailRequest = createRshEmail(emailRequest.getEmail(), emailRequest.getTeamName(), frontendUrl, caseUUID, caseReference, caseStatus);
            emailService.sendRshEmail(sendEmailRequest);
        } else {
            log.warn("Received request to email, but notify request was null!");
        }
    }
}