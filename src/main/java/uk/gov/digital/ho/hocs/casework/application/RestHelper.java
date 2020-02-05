package uk.gov.digital.ho.hocs.casework.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.casework.client.documentclient.S3Document;

import java.nio.charset.Charset;
import java.util.Base64;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Slf4j
@Component
public class RestHelper {

    private String basicAuth;

    private RestTemplate restTemplate;

    private RequestData requestData;

    @Autowired
    public RestHelper(RestTemplate restTemplate, @Value("${hocs.basicauth}") String basicAuth, RequestData requestData) {
        this.restTemplate = restTemplate;
        this.basicAuth = basicAuth;
        this.requestData = requestData;
    }

    @Retryable(maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.delay}"))
    public <T, R> R post(String serviceBaseURL, String url, T request, Class<R> responseType) {
        log.info("RestHelper making POST request to {}{}", serviceBaseURL, url, value(EVENT, REST_HELPER_POST));
        ResponseEntity<R> response = restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.POST, new HttpEntity<>(request, createAuthHeaders()), responseType);
        return response.getBody();
    }

    @Retryable(maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.delay}"))
    public <T, R> R post(String serviceBaseURL, String url, T request, ParameterizedTypeReference<R> responseType) {
        log.info("RestHelper making POST request to {}{}", serviceBaseURL, url, value(EVENT, REST_HELPER_POST));
        ResponseEntity<R> response = restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.POST, new HttpEntity<>(request, createAuthHeaders()), responseType);
        return response.getBody();
    }

    @Retryable(maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.delay}"), exclude = HttpClientErrorException.NotFound.class)
    public <R> R get(String serviceBaseURL, String url, Class<R> responseType) {
        log.info("RestHelper making Get request to {}{}", serviceBaseURL, url, value(EVENT, REST_HELPER_GET));
        ResponseEntity<R> response = restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.GET, new HttpEntity<>(null, createAuthHeaders()), responseType);
        return response.getBody();
    }

    @Retryable(maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.delay}"))
    public void delete(String serviceBaseURL, String url) {
        log.info("RestHelper making Delete request to {}{}", serviceBaseURL, url, value(EVENT, REST_HELPER_DELETE));
        restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.DELETE, new HttpEntity<>(null, createAuthHeaders()), String.class);
    }

    @Retryable(maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.delay}"))
    public S3Document getFile(String serviceBaseURL, String url) {
        log.info("RestHelper making Get request for document {}{}", serviceBaseURL, url, value(EVENT, REST_HELPER_GET));
        ResponseEntity<ByteArrayResource> response = restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.GET, new HttpEntity<>(null, createAuthHeaders()), ByteArrayResource.class);
        ByteArrayResource body = response.getBody();
        String filename = getFilename(response);
        String originalFilename = filename;
        String fileType = getFileExtension(filename);
        String mimeType = response.getHeaders().getContentType().toString();
        S3Document document = new S3Document(filename, originalFilename, body.getByteArray(), fileType, mimeType);
        return document;
    }

    @Retryable(maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.delay}"), exclude = HttpClientErrorException.NotFound.class)
    public <R> R get(String serviceBaseURL, String url, ParameterizedTypeReference<R> responseType) {
        log.info("RestHelper making Get request to {}{}", serviceBaseURL, url, value(EVENT, REST_HELPER_GET));
        ResponseEntity<R> response = restTemplate.exchange(String.format("%s%s", serviceBaseURL, url), HttpMethod.GET, new HttpEntity<>(null, createAuthHeaders()), responseType);
        return response.getBody();
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(AUTHORIZATION, getBasicAuth());
        headers.add(RequestData.GROUP_HEADER, requestData.groups());
        headers.add(RequestData.USER_ID_HEADER, requestData.userId());
        headers.add(RequestData.CORRELATION_ID_HEADER, requestData.correlationId());
        return headers;
    }

    private String getBasicAuth() {
        return String.format("Basic %s", Base64.getEncoder().encodeToString(basicAuth.getBytes(Charset.forName("UTF-8"))));
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    private String getFilename(ResponseEntity<ByteArrayResource> response) {
        if (response.getHeaders().containsKey(HttpHeaders.CONTENT_DISPOSITION)) {
            ContentDisposition contentDisposition = ContentDisposition.parse(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0));
            return contentDisposition.getFilename();
        }
        return null;
    }

}
