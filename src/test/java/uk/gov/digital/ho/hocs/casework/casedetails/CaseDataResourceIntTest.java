package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.HocsCaseApplication;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateStageResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HocsCaseApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CaseDataResourceIntTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private UUID caseUUID;
    private UUID stageUUID;

    @Before
    public void setup() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, String> body = buildCreateCaseBody();
        HttpEntity<?> caseHttpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity<CreateCaseResponse> caseResponseEntity = restTemplate.postForEntity("/case", caseHttpEntity, CreateCaseResponse.class);
        assertThat(caseResponseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(caseResponseEntity.getBody()).hasFieldOrProperty("uuid");
        caseUUID = caseResponseEntity.getBody().getUuid();
        assertThat(caseUUID).isNotNull();

        Map<String, Object> stageBody = buildCreateStageBody();
        HttpEntity<?> stageHttpEntity = new HttpEntity<Object>(stageBody, requestHeaders);
        ResponseEntity<CreateStageResponse> stageResponseEntity = restTemplate.postForEntity("/case/" + caseUUID + "/stage", stageHttpEntity, CreateStageResponse.class);
        assertThat(stageResponseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(stageResponseEntity.getBody()).hasFieldOrProperty("uuid");
        stageUUID = stageResponseEntity.getBody().getUuid();
        assertThat(stageUUID).isNotNull();
    }

    @Test
    public void shouldCreateCase() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, String> body = buildCreateCaseBody();

        // override caseUUID
        body.put("uuid", UUID.randomUUID().toString());

        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity<CreateCaseResponse> responseEntity = restTemplate.postForEntity("/case", httpEntity, CreateCaseResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON_UTF8);
        assertThat(responseEntity.getBody()).hasFieldOrProperty("uuid");
        assertThat(responseEntity.getBody()).hasFieldOrProperty("reference");
    }

    @Test
    public void shouldReturnBadRequestWhenBodyMissingOnCreateCase() {
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
    public void shouldReturnBadRequestWhenCaseTypeInBodyIsIncorrectOnCreateCase() {
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
                "/case/" + caseUUID,
                HttpMethod.PUT,
                httpEntity,
                String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    }

    @Ignore("We don't do anything on Update that the moment")
    @Test
    public void shouldReturnBadRequestWhenBodyMissingOnUpdateCase() {
        HttpHeaders requestHeaders = buildHttpHeaders();

        HttpEntity<?> httpEntity = new HttpEntity<Object>(new HashMap<>(), requestHeaders);

        ResponseEntity responseEntity = restTemplate.exchange(
                "/case/" + caseUUID,
                HttpMethod.PUT,
                httpEntity,
                String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    private Map<String, Object> buildCreateStageBody() {
        Map<String, String> stageData = new HashMap<>();
        stageData.put("A","A1");
        stageData.put("B","B1");
        Map<String, Object> body = new HashMap<>();
        body.put("type", "DCU_MIN_MARKUP");
        body.put("data", stageData);
        return body;
    }

    private Map<String, String> buildCreateCaseBody() {
        Map<String, String> body = new HashMap<>();
        body.put("type", "MIN");
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

}
