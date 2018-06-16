package uk.gov.digital.ho.hocs.casework.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.email.dto.SendEmailRequest;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Service
@Slf4j
class ProxyingNotificationClient extends NotificationClient {

    ProxyingNotificationClient(@Value("${notify.apiKey}") String apiKey,
                               @Value("${notify.proxy.host}") String proxyHost,
                               @Value("${notify.proxy.port}") Integer proxyPort) {
        super(apiKey,
                proxyHost != null ?
                        new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)) :
                        null);
    }

    void sendEmail(SendEmailRequest sendEmailRequest, String templateId) {
        try {
            sendEmail(templateId, sendEmailRequest.getEmailAddress(), sendEmailRequest.getPersonalisation(), null);
        } catch (NotificationClientException e) {
            e.printStackTrace();
        }
    }

}
