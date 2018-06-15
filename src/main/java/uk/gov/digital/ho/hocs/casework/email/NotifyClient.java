package uk.gov.digital.ho.hocs.casework.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.email.dto.model.Email;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.digital.ho.hocs.casework.HocsCaseApplication.isNullOrEmpty;

@Service
@Slf4j
class NotifyClient {

    private final NotificationClient client;

    public NotifyClient(@Value("${notify.apiKey}") String apiKey,
                        @Value("${notify.proxy.host}") String proxyHost,
                        @Value("${notify.proxy.port}") Integer proxyPort) {

        if (!isNullOrEmpty(proxyHost) && proxyPort != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            client = new NotificationClient(apiKey, proxy);
            log.info("Creating Notify client with proxy configuration");
        } else {
            client = new NotificationClient(apiKey);
            log.info("Creating Notify client without proxy configuration");
        }
    }

    void sendEmail(Email email, String frontEndUrl) {

        Map<String, String> personalisation = createPersonalisationMap(email, frontEndUrl);
        try {
            log.debug("Sending email to {} {}, templateId {}", email.getEmailAddress(), email.getTeamName(), email.getTemplateId());
            client.sendEmail(email.getTemplateId(), email.getEmailAddress(), personalisation, null, null);
        } catch (NotificationClientException e) {
            log.warn("Sending email to {} {}, templateId {} failed!", email.getEmailAddress(), email.getTeamName(), email.getTemplateId());
            e.printStackTrace();
        }
    }

    private Map<String, String> createPersonalisationMap(Email email, String frontEndUrl) {
        Map<String, String> personalisation = new HashMap<>();
        personalisation.put("team", email.getTeamName());
        personalisation.put("link", frontEndUrl + "/case/" + email.getCaseUUID());
        personalisation.put("reference", email.getCaseReference());
        personalisation.put("caseStatus", email.getCaseStatus());
        return personalisation;
    }

}
