package uk.gov.digital.ho.hocs.casework.rsh;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.CaseDataService;
import uk.gov.digital.ho.hocs.casework.casedetails.InputDataService;
import uk.gov.digital.ho.hocs.casework.casedetails.StageDataService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
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
    private final InputDataService inputDataService;
    private final EmailService emailService;

    private final String frontendUrl;

    @Autowired
    public RshCaseService(CaseDataService caseDataService,
                          StageDataService stageDataService,
                          InputDataService inputDataService,
                          EmailService emailService,
                          @Value("${notify.frontend.url}") String frontEndUrl) {
        this.caseDataService = caseDataService;
        this.stageDataService = stageDataService;
        this.inputDataService = inputDataService;
        this.emailService = emailService;
        this.frontendUrl = frontEndUrl;
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
    public CaseData createRshCase(Map<String, String> data, SendRshEmailRequest emailRequest) {
        if (data != null) {
            CaseData caseData = caseDataService.createCase(CaseType.RSH);
            if (caseData != null) {
                stageDataService.createStage(caseData.getUuid(), StageType.RUSH_ONLY_STAGE, null, null);
                inputDataService.createInputData(caseData.getUuid());
                inputDataService.updateInputData(caseData.getUuid(), data);
                sendRshEmail(emailRequest, caseData.getUuid(), caseData.getReference(), data.get("outcome"));
                return caseData;
            } else {
                throw new EntityNotFoundException("Failed to create case!");
            }
        } else {
            throw new EntityCreationException("Failed to create case, no data!");
        }
    }

    @Transactional
    public CaseData updateRshCase(UUID caseUUID, Map<String, String> data, SendRshEmailRequest emailRequest) {
        if (data != null) {
            CaseData caseData = caseDataService.getCase(caseUUID);
            if (caseData != null) {
                inputDataService.updateInputData(caseUUID, data);
                sendRshEmail(emailRequest, caseData.getUuid(), caseData.getReference(), data.get("outcome"));
                return caseData;
            } else {
                throw new EntityNotFoundException("Failed to update case!");
            }
        } else {
            throw new EntityCreationException("Failed to create case, no data!");
        }
    }

    private void sendRshEmail(SendRshEmailRequest emailRequest, UUID caseUUID, String caseReference, String caseStatus) {
        if (emailRequest != null) {
            SendEmailRequest sendEmailRequest = createRshEmail(emailRequest.getEmail(), emailRequest.getTeamName(), frontendUrl, caseUUID, caseReference, caseStatus);
            emailService.sendRshEmail(sendEmailRequest);
        } else {
            log.info("Received request to email, but notify request was null!");
        }
    }

    private SendEmailRequest createRshEmail(String emailAddress, String teamName, String frontEndUrl, UUID caseUUID, String caseReference, String caseStatus) {
        Map<String, String> personalisation = new HashMap<>();
        personalisation.put("team", teamName);
        personalisation.put("link", frontEndUrl + "/case/" + caseUUID);
        personalisation.put("reference", caseReference);
        personalisation.put("caseStatus", caseStatus);

        return new SendEmailRequest(emailAddress, personalisation);
    }
}