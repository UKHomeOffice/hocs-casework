package uk.gov.digital.ho.hocs.casework.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.email.dto.model.Email;
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
        super(apiKey, new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
    }

    void sendEmail(Email email) {
        try {
            sendEmail(email.getTemplateId(), email.getEmailAddress(), email.getPersonalisation(), null, null);
        } catch (NotificationClientException e) {
            e.printStackTrace();
        }
    }


}
