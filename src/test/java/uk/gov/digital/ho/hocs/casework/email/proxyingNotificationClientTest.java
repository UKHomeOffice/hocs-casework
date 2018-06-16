package uk.gov.digital.ho.hocs.casework.email;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class proxyingNotificationClientTest {

    @Test
    public void shouldUseProxyTest() {
        String apiKey = "a key";
        String proxyHost = "somewhere";
        Integer proxyPort = 0;

        ProxyingNotificationClient proxyingNotificationClient = new ProxyingNotificationClient(apiKey, proxyHost, proxyPort);

        assertThat(proxyingNotificationClient.getProxy()).isNotNull();
    }

    @Test
    public void shouldNotUseProxyTest() {
        String apiKey = "a key";
        String proxyHost = null;
        Integer proxyPort = 0;

        ProxyingNotificationClient proxyingNotificationClient = new ProxyingNotificationClient(apiKey, proxyHost, proxyPort);

        assertThat(proxyingNotificationClient.getProxy()).isNull();
    }
}
