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
public class StageDataResourceIntTest {

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
    public void shouldCreateStage() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> stageBody = buildCreateStageBody();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(stageBody, requestHeaders);

        // override caseUUID
        stageBody.put("uuid", UUID.randomUUID().toString());

        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                "/case/" + caseUUID + "/stage",
                HttpMethod.POST,
                httpEntity,
                Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void shouldReturnBadRequestWhenBodyMissingOnCreateStage() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> body = new HashMap<>();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                "/case/" + caseUUID + "/stage",
                HttpMethod.POST,
                httpEntity,
                Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void shouldReturnBadRequestWhenCaseTypeInBodyIsIncorrectOnCreateStage() {
        HttpHeaders requestHeaders = buildHttpHeaders();

        Map<String, String> body = new HashMap<>();
        body.put("StsgeType", "wrong");
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                "/case/" + caseUUID + "/stage",
                HttpMethod.POST,
                httpEntity,
                Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void shouldUpdateStage() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> stageBody = buildCreateStageBody();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(stageBody, requestHeaders);

        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                "/case/" + caseUUID + "/stage/" + stageUUID,
                HttpMethod.PUT,
                httpEntity,
                Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

    }

    @Test
    public void shouldReturnBadRequestWhenBodyMissingOnUpdateStage() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> body = new HashMap<>();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                "/case/" + caseUUID + "/stage/" + stageUUID,
                HttpMethod.PUT,
                httpEntity,
                Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void shouldReturnBadRequestWhenCaseTypeInBodyIsIncorrectOnUpdateStage() {
        HttpHeaders requestHeaders = buildHttpHeaders();

        Map<String, String> body = new HashMap<>();
        body.put("StageType", "wrong");
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                "/case/" + caseUUID + "/stage/" + stageUUID,
                HttpMethod.PUT,
                httpEntity,
                Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }


    private Map<String, Object> buildCreateStageBody() {
        Map<String, String> stageData = new HashMap<>();
        stageData.put("A", "A1");
        stageData.put("B", "B1");
        Map<String, Object> body = new HashMap<>();
        body.put("type", "DCU_MIN_CATEGORISE");
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
