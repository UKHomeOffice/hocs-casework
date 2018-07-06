package uk.gov.digital.ho.hocs.casework.caseDetails;

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
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.CreateCaseResponse;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HocsCaseApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DocumentResourceIntTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<CreateCaseResponse> createCaseResponse;
    private UUID caseUUID;

    @Before
    public void setup() {
        caseUUID = UUID.randomUUID();
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, String> body = buildCreateCaseBody();
        HttpEntity<?> caseHttpEntity = new HttpEntity<Object>(body, requestHeaders);
        createCaseResponse = restTemplate.postForEntity(
                "/case",
                caseHttpEntity,
                CreateCaseResponse.class);
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
    public void shouldAddDocuments() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, List<Object>> documentsBody = buildCreateDocumentsBody();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(documentsBody, requestHeaders);

        ResponseEntity responseEntity = restTemplate.exchange(
                "/case/" + caseUUID + "/documents",
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
    public void shouldReturnBadRequestWhenBodyMissingOnAddDocuments(){
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> documentBody = new HashMap<>();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(documentBody, requestHeaders);

        ResponseEntity responseEntity = restTemplate.exchange(
                "/case/" + caseUUID + "/documents",
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

    @Test
    public void shouldReturnOKWhenAddDocumentsContainsDocumentUUIDAlreadyInDatabase() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, List<Object>> documentsBody = buildCreateDocumentsBodyWithDuplicate();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(documentsBody, requestHeaders);

        ResponseEntity responseEntity = restTemplate.exchange(
                "/case/" + caseUUID + "/documents",
                HttpMethod.POST,
                httpEntity,
                Void.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    }

    private Map<String, String> buildCreateCaseBody() {
        Map<String, String> body = new HashMap<>();
        body.put("caseType", "MIN");
        body.put("caseUUID", caseUUID.toString());
        return body;
    }

    private Map<String, Object> buildCreateDocumentBody() {
        Map<String, String> documentData = buildDocumentDataMap();
        Map<String, Object> body = new HashMap<>();
        body.put("documentSummary",documentData);

        return body;
    }

    private Map<String,List<Object>> buildCreateDocumentsBody() {
        Map<String, String> documentData = buildDocumentDataMap();
        Map<String, String> documentData1 = buildDocumentDataMap();
        Map<String, String> documentData2 = buildDocumentDataMap();
        List<Object> documentsList = new ArrayList<>();
        documentsList.add(documentData);
        documentsList.add(documentData1);
        documentsList.add(documentData2);
        Map<String, List<Object>> body = new HashMap<>();
        body.put("documentSummaries", documentsList);
        return body;
    }

    private Map<String,List<Object>> buildCreateDocumentsBodyWithDuplicate() {
        Map<String, String> documentData = buildDocumentDataMap();
        Map<String, String> documentData1 = buildDocumentDataMap();
        Map<String, String> documentData2 = buildDocumentDataMap();
        List<Object> documentsList = new ArrayList<>();
        documentsList.add(documentData);
        documentsList.add(documentData1);
        documentsList.add(documentData1);
        documentsList.add(documentData2);
        Map<String, List<Object>> body = new HashMap<>();
        body.put("documentSummaries", documentsList);
        return body;
    }

    private Map<String, String> buildDocumentDataMap() {
        Map<String, String> documentData = new HashMap<>();
        documentData.put("UUID",UUID.randomUUID().toString());
        documentData.put("displayName","A Response File");
        documentData.put("type","FINAL_RESPONSE");
        return documentData;
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