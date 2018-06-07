package uk.gov.digital.ho.hocs.casework.notify;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.service.notify.NotificationClient;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Service
@Slf4j
public class NotifyClient {

    @Getter
    private final NotificationClient client;

    @Autowired
    public NotifyClient(@Value("${notify.apiKey}") String apiKey,
                         @Value("${notify.proxy.host}") String proxyHost,
                         @Value("${notify.proxy.port}") Integer proxyPort) {

        if (proxyHost != null && proxyPort != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            client = new NotificationClient(apiKey, proxy);
            log.info("Proxy configuration applied to Notify client");
        } else {
            client = new NotificationClient(apiKey);
        }
    }

}
