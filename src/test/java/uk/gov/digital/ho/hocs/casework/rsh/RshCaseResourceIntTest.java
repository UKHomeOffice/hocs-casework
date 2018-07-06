package uk.gov.digital.ho.hocs.casework.rsh;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.HocsCaseApplication;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.CreateStageResponse;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = HocsCaseApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RshCaseResourceIntTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<CreateCaseResponse> createCaseResponse;

    @Before
    public void setup() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Map<String, String>> body = buildCreateCaseBody();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        createCaseResponse = restTemplate.postForEntity("/rsh/case", httpEntity, CreateCaseResponse.class);
    }


    @Test
    public void shouldCreateRshCase() {

        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Map<String, String>> body = buildCreateCaseBody();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity<CreateCaseResponse> responseEntity = restTemplate.postForEntity("/rsh/case", httpEntity, CreateCaseResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON_UTF8);
        assertThat(responseEntity.getBody()).hasFieldOrProperty("caseReference");
        assertThat(responseEntity.getBody()).hasFieldOrProperty("uuid");
    }

    @Test
    public void shouldUpdateRshCase() {

        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Map<String, String>> body = new HashMap<>();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity<CreateCaseResponse> responseEntity = restTemplate.postForEntity("/rsh/case/" + createCaseResponse.getBody().getUuid(), httpEntity, CreateCaseResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON_UTF8);
        assertThat(responseEntity.getBody()).hasFieldOrProperty("caseReference");
        assertThat(responseEntity.getBody()).hasFieldOrProperty("uuid");
    }

    @Test
    public void shouldReturnRshCaseDetails() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Map<String, String>> body = new HashMap<>();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);

        ResponseEntity responseEntity = restTemplate.exchange(
                "/rsh/case/" + createCaseResponse.getBody().getUuid(),
                HttpMethod.GET,
                httpEntity,
                CaseData.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody()).hasFieldOrProperty("stages");
        assertThat(responseEntity.getBody()).hasFieldOrPropertyWithValue("type", "RSH");
        assertThat(responseEntity.getBody()).hasFieldOrPropertyWithValue("reference", createCaseResponse.getBody().getCaseReference());
        assertThat(responseEntity.getBody()).hasFieldOrPropertyWithValue("uuid", createCaseResponse.getBody().getUuid());
        assertThat(responseEntity.getBody()).hasFieldOrProperty("timestamp");
    }

    private Map<String, Map<String, String>> buildCreateCaseBody() {
        Map<String, String> caseData = new HashMap<>();
        caseData.put("Name", "RSH");
        Map<String, Map<String, String>> body = new HashMap<>();
        body.put("caseData", caseData);
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
