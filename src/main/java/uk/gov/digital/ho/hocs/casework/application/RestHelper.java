package uk.gov.digital.ho.hocs.casework.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.Base64;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class RestHelper {

    private String basicAuth;

    private RestTemplate restTemplate;

    @Autowired
    public RestHelper(RestTemplate restTemplate, @Value("${hocs.basicauth}") String basicAuth) {
        this.restTemplate = restTemplate;
        this.basicAuth = basicAuth;
    }

    public <R> ResponseEntity<R> get(String serviceBaseURL, String url, Class<R> responseType) {
        return restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.GET, new HttpEntity<>(null, createAuthHeaders()), responseType);
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(AUTHORIZATION, getBasicAuth());
        return headers;
    }

    private String getBasicAuth() {
        return String.format("Basic %s", Base64.getEncoder().encodeToString(basicAuth.getBytes(Charset.forName("UTF-8"))));
    }

}
