package uk.gov.digital.ho.hocs.casework.caseDetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import uk.gov.digital.ho.hocs.casework.HocsCaseApplication;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.CreateStageResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = HocsCaseApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CaseDataResourceIntTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<CreateCaseResponse> createCaseResponse;
    private ResponseEntity<CreateStageResponse> createStageResponse;

    @Before
    public void setup() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, String> body = buildCreateCaseBody();
        HttpEntity<?> caseHttpEntity = new HttpEntity<Object>(body, requestHeaders);
        createCaseResponse = restTemplate.postForEntity(
                "/case",
                caseHttpEntity,
                CreateCaseResponse.class);
        Map<String, Object> stageBody = buildCreateStageBody();
        HttpEntity<?> stageHttpEntity = new HttpEntity<Object>(stageBody, requestHeaders);
        createStageResponse = restTemplate.postForEntity(
                "/case/" + createCaseResponse.getBody().getUuid() + "/stage",
                stageHttpEntity,
                CreateStageResponse.class);

    }

    @Test
    public void shouldCreateCase() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, String> body = buildCreateCaseBody();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity<CreateCaseResponse> responseEntity = restTemplate.postForEntity("/case", httpEntity, CreateCaseResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON_UTF8);
        assertThat(responseEntity.getBody()).hasFieldOrProperty("caseReference");
        assertThat(responseEntity.getBody()).hasFieldOrProperty("uuid");
    }

    @Test
    public void ShouldReturnBadRequestWhenBodyMissingOnCreateCase() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, String> body = new HashMap<>();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity<CreateCaseResponse> responseEntity = restTemplate.postForEntity(
                "/case",
                httpEntity,
                CreateCaseResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void ShouldReturnBadRequestWhenCaseTypeInBodyIsIncorrectOnCreateCase() {
        HttpHeaders requestHeaders = buildHttpHeaders();

        Map<String, String> body = new HashMap<>();
        body.put("caseType", "wrong");
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity<CreateCaseResponse> responseEntity = restTemplate.postForEntity(
                "/case",
                httpEntity,
                CreateCaseResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void shouldUpdateCase() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, String> body = buildCreateCaseBody();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity responseEntity = restTemplate.exchange(
                "/case/" + createCaseResponse.getBody().getUuid(),
                HttpMethod.PUT,
                httpEntity,
                String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void shouldReturnBadRequestWhenBodyMissingOnUpdateCase() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, String> body = new HashMap<>();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity responseEntity = restTemplate.exchange(
                "/case/" + createCaseResponse.getBody().getUuid(),
                HttpMethod.PUT,
                httpEntity,
                String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void shouldReturnBadRequestWhenCaseTypeInBodyIsIncorrectOnUpdateCase() {
        HttpHeaders requestHeaders = buildHttpHeaders();

        Map<String, String> body = new HashMap<>();
        body.put("caseType", "wrong");
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity responseEntity = restTemplate.exchange(
                "/case/" + createCaseResponse.getBody().getUuid(),
                HttpMethod.PUT,
                httpEntity,
                String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void shouldCreateStage() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> stageBody = buildCreateStageBody();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(stageBody, requestHeaders);

        ResponseEntity<CreateStageResponse> responseEntity = restTemplate.exchange(
                "/case/" + createCaseResponse.getBody().getUuid() + "/stage",
                HttpMethod.POST,
                httpEntity,
                CreateStageResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON_UTF8);
        assertThat(responseEntity.getBody()).hasFieldOrProperty("uuid");

    }

    @Test
    public void shouldReturnBadRequestWhenBodyMissingOnCreateStage() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> body = new HashMap<>();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity<CreateStageResponse> responseEntity = restTemplate.exchange(
                "/case/" + createCaseResponse.getBody().getUuid() + "/stage",
                HttpMethod.POST,
                httpEntity,
                CreateStageResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void shouldReturnBadRequestWhenCaseTypeInBodyIsIncorrectOnCreateStage() {
        HttpHeaders requestHeaders = buildHttpHeaders();

        Map<String, String> body = new HashMap<>();
        body.put("StsgeType", "wrong");
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity<CreateStageResponse> responseEntity = restTemplate.exchange(
                "/case/" + createCaseResponse.getBody().getUuid() + "/stage",
                HttpMethod.POST,
                httpEntity,
                CreateStageResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void shouldUpdateStage() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> stageBody = buildCreateStageBody();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(stageBody, requestHeaders);

        ResponseEntity<CreateStageResponse> responseEntity = restTemplate.exchange(
                "/case/" + createCaseResponse.getBody().getUuid() + "/stage/" + createStageResponse.getBody().getUuid(),
                HttpMethod.PUT,
                httpEntity,
                CreateStageResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

    }

    @Test
    public void shouldReturnBadRequestWhenBodyMissingOnUpdateStage() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> body = new HashMap<>();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity<CreateStageResponse> responseEntity = restTemplate.exchange(
                "/case/" + createCaseResponse.getBody().getUuid() + "/stage/" + createStageResponse.getBody().getUuid(),
                HttpMethod.PUT,
                httpEntity,
                CreateStageResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void shouldReturnBadRequestWhenCaseTypeInBodyIsIncorrectOnUpdateStage() {
        HttpHeaders requestHeaders = buildHttpHeaders();

        Map<String, String> body = new HashMap<>();
        body.put("StsgeType", "wrong");
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity<CreateStageResponse> responseEntity = restTemplate.exchange(
                "/case/" + createCaseResponse.getBody().getUuid() + "/stage/" + createStageResponse.getBody().getUuid(),
                HttpMethod.PUT,
                httpEntity,
                CreateStageResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }


    private Map<String, Object> buildCreateStageBody() {
        Map<String, String> stageData = new HashMap<>();
        stageData.put("A","A1");
        stageData.put("B","B1");
        Map<String, Object> body = new HashMap<>();
        body.put("stageType","DCU_MIN_CATEGORISE");
        body.put("stageData",stageData);
        return body;
    }

    private Map<String, String> buildCreateCaseBody() {
        Map<String, String> body = new HashMap<>();
        body.put("caseType", "MIN");
        return body;
    }

    private HttpHeaders buildHttpHeaders() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        requestHeaders.set("X-Auth-Userid", "1");
        requestHeaders.set("X-Auth-Username", "bob");
        requestHeaders.set("x-correlation-id", "12");
        return requestHeaders;
    }


    private String buildCreateCaseServiceResponse() throws IOException {
        return "{ \"caseReference\": \"MIN/0020025/18\",\"uuid\": \"04f4594d-14bf-42be-baf2-9b87540a0b6b\" }";
    }

}
