package uk.gov.digital.ho.hocs.casework.rsh;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.RequestData;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseDataService;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageType;
import uk.gov.digital.ho.hocs.casework.email.EmailService;
import uk.gov.digital.ho.hocs.casework.email.dto.SendEmailRequest;
import uk.gov.digital.ho.hocs.casework.rsh.dto.SendRshEmailRequest;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.HocsCaseApplication.isNullOrEmpty;

@Service
@Slf4j
public class RshCaseService {

    private final CaseDataService caseDataService;
    private final EmailService emailService;

    private final String frontendUrl;
    private final String rshTemplateId;

    @Autowired
    public RshCaseService(CaseDataService caseDataService,
                          EmailService emailService,
                          @Value("${notify.frontend.url}") String frontEndUrl,
                          @Value("${notify.rshTemplateId}") String rshTemplateId) {
        this.caseDataService = caseDataService;
        this.emailService = emailService;
        this.frontendUrl = frontEndUrl;
        this.rshTemplateId = rshTemplateId;
    }

    private static SendEmailRequest createRshEmail(String emailAddress, String teamName, String frontEndUrl, UUID caseUUID, String caseReference, String caseStatus) {
        Map<String, String> personalisation = new HashMap<>();
        personalisation.put("team", teamName);
        personalisation.put("link", frontEndUrl + "/case/" + caseUUID);
        personalisation.put("reference", caseReference);
        personalisation.put("caseStatus", caseStatus);

        return new SendEmailRequest(emailAddress, personalisation);
    }

    @Transactional
    CaseData createRshCase(Map<String, String> caseData, SendRshEmailRequest emailRequest) throws EntityCreationException {
        if (caseData != null) {
            CaseData caseDetails = caseDataService.createCase(CaseType.RSH);
            caseDataService.createStage(caseDetails.getUuid(), StageType.DCU_MIN_CATEGORISE, caseData);
            sendRshEmail(emailRequest, caseDetails.getUuid(), caseDetails.getReference(), caseData.get("outcome"));
            return caseDetails;
        } else {
            throw new EntityCreationException("Failed to create case, no caseData!");
        }
    }

    CaseData getRSHCase(UUID caseUUID) throws EntityNotFoundException {
        if (!isNullOrEmpty(caseUUID)) {
            CaseData caseData = caseDataService.getCase(caseUUID);
            if (caseData != null) {
                return caseData;
            } else {
                throw new EntityNotFoundException("Case not found!");
            }
        } else {
            throw new EntityNotFoundException("Failed to get case, no caseUUID!");
        }
    }

    CaseData updateRshCase(UUID caseUUID, Map<String, String> caseData, SendRshEmailRequest emailRequest) throws EntityCreationException, EntityNotFoundException {
        if (!isNullOrEmpty(caseUUID) && caseData != null) {
            CaseData caseDetails = caseDataService.getCase(caseUUID);
            if (!caseDetails.getStages().isEmpty()) {
                StageData stageData = caseDetails.getStages().iterator().next();
                caseDataService.updateStage(caseUUID, stageData.getUuid(), StageType.DCU_MIN_CATEGORISE, caseData);
                sendRshEmail(emailRequest, caseDetails.getUuid(), caseDetails.getReference(), caseData.get("outcome"));
                return caseDetails;
            } else {
                throw new EntityCreationException("Failed to update case, case has no stages!");
            }
        } else {
            throw new EntityCreationException("Failed to update case, no caseUUID or caseData!");
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