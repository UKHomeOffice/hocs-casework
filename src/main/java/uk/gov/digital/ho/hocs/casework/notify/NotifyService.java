package uk.gov.digital.ho.hocs.casework.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.model.NotifyRequest;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.UUID;


@Service
@Slf4j
public class NotifyService {

    private final NotifyClient notify;

    private final String rshTemplateId;
    private final String frontEndUrl;

    @Autowired
    public NotifyService(NotifyClient notifyClient,
                         @Value("${notify.rshTemplateId}") String rshTemplateId,
                         @Value("${notify.frontend.url}") String frontEndUrl) {
        this.notify = notifyClient;
        this.rshTemplateId = rshTemplateId;
        this.frontEndUrl = frontEndUrl;
    }

    public void sendRshNotify(NotifyRequest notifyRequest, UUID caseUUID, String reference, String caseStatus) {
        sendNotify(notifyRequest, caseUUID, reference, caseStatus, rshTemplateId);
    }

    public void sendNotify(NotifyRequest notifyRequest, UUID caseUUID, String reference, String caseStatus ,String templateId) {
        log.info("Received request to email {} {}, templateId {}", notifyRequest.getNotifyEmail(), notifyRequest.getNotifyEmail(), templateId);
        if (notifyRequest.getNotifyEmail() != null && !notifyRequest.getNotifyEmail().isEmpty()) {
            sendEmail(notifyRequest.getNotifyEmail(), notifyRequest.getNotifyTeamName(), caseUUID, reference, caseStatus, templateId);
        }
    }

    private void sendEmail(String emailAddress, String teamName, UUID caseUUID, String reference, String caseStatus ,String templateId) {
        log.info("Sending email to {} {}, templateId {}", emailAddress, teamName, templateId);
        HashMap<String, String> personalisation = new HashMap<>();
        personalisation.put("team", teamName);
        personalisation.put("link", frontEndUrl + "/case/" + caseUUID);
        personalisation.put("reference", reference);
        personalisation.put("caseStatus", caseStatus);
        try {
            notify.getClient().sendEmail(templateId, emailAddress, personalisation, null, null);
        } catch (NotificationClientException e) {
            e.printStackTrace();
        }
    }
}

