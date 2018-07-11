package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.After;
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
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.DocumentRepository;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.StageDataRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HocsCaseApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DocumentResourceIntTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private CaseDataRepository caseDataRepository;
    @Autowired
    private StageDataRepository stageDataRepository;
    @Autowired
    private DocumentRepository documentRepository;

    private UUID caseUUID;
    private UUID stageUUID;

    @Before
    public void setup() {
        clearDatabase();
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
    public void shouldAddDocument() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> documentBody = buildCreateDocumentBody();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(documentBody, requestHeaders);

        ResponseEntity responseEntity = restTemplate.exchange(
                "/case/" + caseUUID + "/document",
                HttpMethod.POST,
                httpEntity,
                Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void shouldReturnBadRequestWhenBodyMissingOnAddDocument(){
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> documentBody = new HashMap<>();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(documentBody, requestHeaders);

        ResponseEntity responseEntity = restTemplate.exchange(
                "/case/" + caseUUID + "/document",
                HttpMethod.POST,
                httpEntity,
                Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void shouldReturnOKWhenDocumentUUIDAddedSecondTime(){
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> documentBody = buildCreateDocumentBody();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(documentBody, requestHeaders);

        ResponseEntity responseEntity = restTemplate.exchange(
                "/case/" + caseUUID + "/document",
                HttpMethod.POST,
                httpEntity,
                Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

        ResponseEntity responseEntityDuplicate = restTemplate.exchange(
                "/case/" + caseUUID + "/document",
                HttpMethod.POST,
                httpEntity,
                Void.class);

        assertThat(responseEntityDuplicate.getStatusCode()).isEqualTo(OK);
    }

    @After
    public void tearDown() {
        clearDatabase();
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

    private Map<String, Object> buildCreateDocumentBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "A Response File");
        body.put("type", "FINAL_RESPONSE");

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

    private void clearDatabase() {
        stageDataRepository.deleteAll();
        documentRepository.deleteAll();
        caseDataRepository.deleteAll();
    }
}