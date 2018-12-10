package uk.gov.digital.ho.hocs.casework.client.notifiyclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoNominatedPeople;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class NotifyClient {

    private final NotificationClient notifyClient;
    private final InfoClient infoClient;
    private final String URL;

    @Autowired
    public NotifyClient(InfoClient infoClient,
                        @Value("${notify.apiKey}") String apiKey,
                        @Value("${hocs.url}") String URL) {
        this.notifyClient = new NotificationClient(apiKey);
        this.infoClient = infoClient;
        this.URL = URL;
    }

    public void sendTeamEmail(UUID caseUUID, UUID stageUUID, UUID teamUUID, String caseReference, NotifyType notifyType) {
        Set<InfoNominatedPeople> nominatedPeople = infoClient.getNominatedPeople(teamUUID);
        for (InfoNominatedPeople contact : nominatedPeople) {
            sendEmail(caseUUID, stageUUID, contact.getEmailAddress(), caseReference, notifyType);
        }
    }

    public void sendUserEmail(UUID caseUUID, UUID stageUUID, UUID userUUID, String caseReference, String allocationType) {
        String emailAddress = "SOME USER EMAIL";

        sendEmail(caseUUID, stageUUID, emailAddress, caseReference, notifyType);
    }

    private void sendEmail(UUID caseUUID, UUID stageUUID, String emailAddress, String caseReference, String allocationType) {
        String link = String.format("%s/case/%s/stage/%s", URL, caseUUID, stageUUID);
        Map<String, String> personalisation = new HashMap<>();
        personalisation.put("link", link);
        personalisation.put("caseRef", caseReference);
        sendEmail(notifyType, emailAddress, personalisation);
    }

    private void sendEmail(NotifyType notifyType, String emailAddress, Map<String, String> personalisation) {
        log.info("Received request to sendEmailRequest {}, template Name {}, template ID {}", emailAddress, notifyType, notifyType.getDisplayValue());

        try {

            notifyClient.sendEmail(notifyType.getDisplayValue(), emailAddress, personalisation, null);
        } catch (NotificationClientException e) {
            log.error(e.getLocalizedMessage());
            log.warn("Didn't send email to {}", emailAddress);
        }

    }
}
