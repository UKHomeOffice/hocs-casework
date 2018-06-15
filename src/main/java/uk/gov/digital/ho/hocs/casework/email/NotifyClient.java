package uk.gov.digital.ho.hocs.casework.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.UUID;

@Service
@Slf4j
class NotifyClient {

    private final NotificationClient client;

    @Autowired
    public NotifyClient(@Value("${notify.apiKey}") String apiKey,
                        @Value("${notify.proxy.host}") String proxyHost,
                        @Value("${notify.proxy.port}") Integer proxyPort) {

        if (proxyHost != null && proxyPort != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            client = new NotificationClient(apiKey, proxy);
            log.info("Creating Notify client with proxy configuration");
        } else {
            client = new NotificationClient(apiKey);
            log.info("Creating Notify client without proxy configuration");
        }
    }

    void sendEmail(String emailAddress, String teamName, String frontEndUrl, UUID caseUUID, String reference, String caseStatus, String templateId) {
        HashMap<String, String> personalisation = new HashMap<>();
        personalisation.put("team", teamName);
        personalisation.put("link", frontEndUrl + "/case/" + caseUUID);
        personalisation.put("reference", reference);
        personalisation.put("caseStatus", caseStatus);
        try {
            log.debug("Sending email to {} {}, templateId {}", emailAddress, teamName, templateId);
            client.sendEmail(templateId, emailAddress, personalisation, null, null);
        } catch (NotificationClientException e) {
            log.warn("Sending email to {} {}, templateId {} failed!", emailAddress, teamName, templateId);
            e.printStackTrace();
        }
    }

}
