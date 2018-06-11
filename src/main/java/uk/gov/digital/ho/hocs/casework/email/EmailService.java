package uk.gov.digital.ho.hocs.casework.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.email.dto.SendEmailRequest;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.HocsCaseApplication.isNullOrEmpty;


@Service
@Slf4j
public class EmailService {

    private final AuditService auditService;

    private final NotifyClient notifyClient;

    private final String rshTemplateId;
    private final String frontEndUrl;

    @Autowired
    public EmailService(NotifyClient notifyClient,
                        @Value("${notify.rshTemplateId}") String rshTemplateId,
                        @Value("${notify.frontend.url}") String frontEndUrl,
                        AuditService auditService) {
        this.notifyClient = notifyClient;
        this.rshTemplateId = rshTemplateId;
        this.frontEndUrl = frontEndUrl;
        this.auditService = auditService;
    }

    public void sendRshEmail(SendEmailRequest notifyRequest, UUID caseUUID, String userName) {
        sendNotify(notifyRequest, caseUUID, rshTemplateId, userName);
    }

    private void sendNotify(SendEmailRequest notifyRequest, UUID caseUUID, String templateId, String username) {
        if (notifyRequest != null) {
            auditService.writeSendEmailEvent(username, notifyRequest);
            if (!isNullOrEmpty(notifyRequest.getEmail()) && !isNullOrEmpty(notifyRequest.getTeamName())) {
                log.info("Received request to email {} {}, templateId {}", notifyRequest.getEmail(), notifyRequest.getTeamName(), templateId);
                sendEmail(notifyRequest.getEmail(), notifyRequest.getTeamName(), caseUUID, templateId);
            } else {
                log.warn("Received request to email templateId {}, but params were null!", templateId);
            }
        } else {
            log.warn("Received request to email, but request was null!");
        }
    }

    private void sendEmail(String emailAddress, String teamName, UUID caseUUID, String templateId) {
        HashMap<String, String> personalisation = new HashMap<>();
        personalisation.put("team", teamName);
        personalisation.put("link", frontEndUrl + "/case/" + caseUUID);
        try {
            log.debug("Sending email to {} {}, templateId {}", emailAddress, teamName, templateId);
            notifyClient.getClient().sendEmail(templateId, emailAddress, personalisation, null, null);
        } catch (NotificationClientException e) {
            log.warn("Sending email to {} {}, templateId {} failed!", emailAddress, teamName, templateId);
            e.printStackTrace();
        }
    }


}