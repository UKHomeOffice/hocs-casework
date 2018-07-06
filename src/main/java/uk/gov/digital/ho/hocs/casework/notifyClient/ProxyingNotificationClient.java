package uk.gov.digital.ho.hocs.casework.notifyClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.email.dto.SendEmailRequest;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;

@Slf4j
@Component
public class ProxyingNotificationClient extends NotificationClient {

    ProxyingNotificationClient(@Value("${notify.apiKey}") String apiKey,
                               @Value("${notify.proxy.host}") String proxyHost,
                               @Value("${notify.proxy.port}") Integer proxyPort) {
        super(apiKey,
                proxyHost != null ?
                        new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)) :
                        null);
    }

    public void sendEmail(String emailAddress, Map<String,String> personalisation, String templateId) {
        if (emailAddress != null && personalisation !=null && templateId != null) {
            try {
                sendEmail(templateId, emailAddress, personalisation, null);
            } catch (NotificationClientException e) {
                log.error(e.toString());
            }
        }
    }

}
