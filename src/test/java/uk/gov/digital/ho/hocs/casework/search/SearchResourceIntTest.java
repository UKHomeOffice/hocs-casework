package uk.gov.digital.ho.hocs.casework.search;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.HocsCaseApplication;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateStageResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.DocumentRepository;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.StageDataRepository;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HocsCaseApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SearchResourceIntTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private CaseDataRepository caseDataRepository;
    @Autowired
    private StageDataRepository stageDataRepository;
    @Autowired
    private DocumentRepository documentRepository;

    private UUID caseUUID;
    private UUID caseUUID2;
    private ResponseEntity<CreateCaseResponse> caseResponseEntity;
    private ResponseEntity<CreateCaseResponse> caseResponseEntity2;

    @Before
    public void setup() {

       clearDatabase();
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, String> body = buildCreateCaseBody();
        HttpEntity<?> caseHttpEntity = new HttpEntity<Object>(body, requestHeaders);

        caseResponseEntity = restTemplate.postForEntity("/case", caseHttpEntity, CreateCaseResponse.class);
        assertThat(caseResponseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(caseResponseEntity.getBody()).hasFieldOrProperty("uuid");
        caseUUID = caseResponseEntity.getBody().getUuid();
        assertThat(caseUUID).isNotNull();

        Map<String, Object> stageBody = buildCreateStageBody("John", "Smith", LocalDate.of(1970, 01, 01));
        HttpEntity<?> stageHttpEntity = new HttpEntity<Object>(stageBody, requestHeaders);
        ResponseEntity<CreateStageResponse> stageResponseEntity = restTemplate.postForEntity("/case/" + caseUUID + "/stage", stageHttpEntity, CreateStageResponse.class);
        assertThat(stageResponseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(stageResponseEntity.getBody()).hasFieldOrProperty("uuid");
        UUID stageUUID = stageResponseEntity.getBody().getUuid();
        assertThat(stageUUID).isNotNull();

        caseResponseEntity2 = restTemplate.postForEntity("/case", caseHttpEntity, CreateCaseResponse.class);
        assertThat(caseResponseEntity2.getStatusCode()).isEqualTo(OK);
        assertThat(caseResponseEntity2.getBody()).hasFieldOrProperty("uuid");
        caseUUID2 = caseResponseEntity2.getBody().getUuid();
        assertThat(caseUUID2).isNotNull();

        Map<String, Object> stageBody2 = buildCreateStageBody("John", "Jones", LocalDate.of(1990, 02, 20));
        HttpEntity<?> stageHttpEntity2 = new HttpEntity<Object>(stageBody2, requestHeaders);
        ResponseEntity<CreateStageResponse> stageResponseEntity2 = restTemplate.postForEntity("/case/" + caseUUID2 + "/stage", stageHttpEntity2, CreateStageResponse.class);
        assertThat(stageResponseEntity2.getStatusCode()).isEqualTo(OK);
        assertThat(stageResponseEntity2.getBody()).hasFieldOrProperty("uuid");
        UUID stageUUID2 = stageResponseEntity2.getBody().getUuid();
        assertThat(stageUUID2).isNotNull();
    }

    @Test
    public void shouldNotReturnAnyCaseWhenSearchDetailsAreNull() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> body = buildSearchBodyWithParameters("", "", "");
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
        ResponseEntity<Set<CaseData>> responseEntity = getResponseEntityFromSearchRequest(httpEntity);

        assertThat(responseEntity.getBody().size()).isZero();
    }

    @Test
    public void shouldNotReturnAnyCaseWhenCaseReferenceDoesNotExist() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> body = buildSearchBodyWithParameters("", "", "TRO/0020001/18");
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
        ResponseEntity<Set<CaseData>> responseEntity = getResponseEntityFromSearchRequest(httpEntity);

        assertThat(responseEntity.getBody().size()).isZero();
    }

    @Test
    public void shouldReturnSpecificCaseWhenUsingCaseReference() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> body = buildSearchBodyWithParameters("", "", caseResponseEntity.getBody().getReference());
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
        ResponseEntity<Set<CaseData>> responseEntity = getResponseEntityFromSearchRequest(httpEntity);

        assertThat(responseEntity.getBody().size()).isOne();
        assertThat(responseEntity.getBody().iterator().next().getUuid()).isEqualTo(caseUUID);
        assertThat(responseEntity.getBody().iterator().next().getReference()).isEqualTo(caseResponseEntity.getBody().getReference());
        assertThat(responseEntity.getBody().iterator().next().getType()).isEqualTo(CaseType.MIN.toString());
    }

    @Test
    public void shouldOnlyReturnSpecificCaseUsingCaseReferenceWhenSearchUsingCorrectCaseReferenceButIncorrectFirstName() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> body = buildSearchBodyWithParameters("first-name", "James", caseResponseEntity.getBody().getReference());
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
        ResponseEntity<Set<CaseData>> responseEntity = getResponseEntityFromSearchRequest(httpEntity);
        assertThat(responseEntity.getBody().size()).isOne();
        assertThat(responseEntity.getBody().iterator().next().getUuid()).isEqualTo(caseUUID);
        assertThat(responseEntity.getBody().iterator().next().getReference()).isEqualTo(caseResponseEntity.getBody().getReference());
        assertThat(responseEntity.getBody().iterator().next().getType()).isEqualTo(CaseType.MIN.toString());
    }

    @Test
    public void shouldOnlyReturnSpecificCaseUsingCaseReferenceWhenSearchUsingCorrectCaseReferenceButIncorrectLastName() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> body = buildSearchBodyWithParameters("last-name", "James", caseResponseEntity.getBody().getReference());
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
        ResponseEntity<Set<CaseData>> responseEntity = getResponseEntityFromSearchRequest(httpEntity);
        assertThat(responseEntity.getBody().size()).isOne();
        assertThat(responseEntity.getBody().iterator().next().getUuid()).isEqualTo(caseUUID);
        assertThat(responseEntity.getBody().iterator().next().getReference()).isEqualTo(caseResponseEntity.getBody().getReference());
        assertThat(responseEntity.getBody().iterator().next().getType()).isEqualTo(CaseType.MIN.toString());
    }

    @Test
    public void shouldOnlyReturnSpecificCaseUsingCaseReferenceWhenSearchUsingCorrectCaseReferenceButIncorrectDateOfBirth() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> body = buildSearchBodyWithParameters("date-of-birth", "1975-01-01", caseResponseEntity.getBody().getReference());
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
        ResponseEntity<Set<CaseData>> responseEntity = getResponseEntityFromSearchRequest(httpEntity);
        assertThat(responseEntity.getBody().size()).isOne();
        assertThat(responseEntity.getBody().iterator().next().getUuid()).isEqualTo(caseUUID);
        assertThat(responseEntity.getBody().iterator().next().getReference()).isEqualTo(caseResponseEntity.getBody().getReference());
        assertThat(responseEntity.getBody().iterator().next().getType()).isEqualTo(CaseType.MIN.toString());
    }

    @Test
    public void shouldReturnTwoCasesWhenSearchUsingFirstNameLastNameAndDOB() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> body = buildSearchBodyWithAllParameters();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
        ResponseEntity<Set<CaseData>> responseEntity = getResponseEntityFromSearchRequest(httpEntity);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody().size()).isEqualTo(2);

        List<CaseData> responseEntityAsList = new ArrayList<>(responseEntity.getBody());

        CaseData result1 = responseEntityAsList.stream().filter(x -> caseUUID.equals(x.getUuid())).findAny().orElse(null);
        assertThat(result1.getUuid()).isEqualTo(caseUUID);
        assertThat(result1.getReference()).isEqualTo(caseResponseEntity.getBody().getReference());

        CaseData result2 = responseEntityAsList.stream().filter(x -> caseUUID2.equals(x.getUuid())).findAny().orElse(null);
        assertThat(result2.getUuid()).isEqualTo(caseUUID2);
        assertThat(result2.getReference()).isEqualTo(caseResponseEntity2.getBody().getReference());
    }

    @Test
    public void shouldReturnTwoCasesWhenUsingIncorrectRefButCorrectFirstName() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> body = buildSearchBodyWithParameters("first-name", "John", "TRO/0020001/18");
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
        ResponseEntity<Set<CaseData>> responseEntity = getResponseEntityFromSearchRequest(httpEntity);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody().size()).isEqualTo(2);

        List<CaseData> responseEntityAsList = new ArrayList<>(responseEntity.getBody());

        CaseData result1 = responseEntityAsList.stream().filter(x -> caseUUID.equals(x.getUuid())).findAny().orElse(null);
        assertThat(result1.getUuid()).isEqualTo(caseUUID);
        assertThat(result1.getReference()).isEqualTo(caseResponseEntity.getBody().getReference());

        CaseData result2 = responseEntityAsList.stream().filter(x -> caseUUID2.equals(x.getUuid())).findAny().orElse(null);
        assertThat(result2.getUuid()).isEqualTo(caseUUID2);
        assertThat(result2.getReference()).isEqualTo(caseResponseEntity2.getBody().getReference());
    }

    @Test
    public void shouldReturnOneCasesWhenUsingIncorrectRefButCorrectLastName() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> body = buildSearchBodyWithParameters("last-name", "Smith", "TRO/0020001/18");
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
        ResponseEntity<Set<CaseData>> responseEntity = getResponseEntityFromSearchRequest(httpEntity);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody().size()).isEqualTo(1);
        assertThat(responseEntity.getBody().iterator().next().getUuid()).isEqualTo(caseUUID);
        assertThat(responseEntity.getBody().iterator().next().getReference()).isEqualTo(caseResponseEntity.getBody().getReference());
        assertThat(responseEntity.getBody().iterator().next().getType()).isEqualTo(CaseType.MIN.toString());
    }

    @Test
    public void shouldReturnOneCasesWhenUsingIncorrectRefButCorrectDateOfBirth() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> body = buildSearchBodyWithParameters("date-of-birth", "1970-01-01", "TRO/0020001/18");
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
        ResponseEntity<Set<CaseData>> responseEntity = getResponseEntityFromSearchRequest(httpEntity);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody().size()).isEqualTo(1);
        assertThat(responseEntity.getBody().iterator().next().getUuid()).isEqualTo(caseUUID);
        assertThat(responseEntity.getBody().iterator().next().getReference()).isEqualTo(caseResponseEntity.getBody().getReference());
        assertThat(responseEntity.getBody().iterator().next().getType()).isEqualTo(CaseType.MIN.toString());
    }

    @Test
    public void shouldReturnTwoCasesWhenUsingFirstName() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> body = buildSearchBodyWithParameters("first-name", "John", null);
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
        ResponseEntity<Set<CaseData>> responseEntity = getResponseEntityFromSearchRequest(httpEntity);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody().size()).isEqualTo(2);

        List<CaseData> responseEntityAsList = new ArrayList<>(responseEntity.getBody());

        CaseData result1 = responseEntityAsList.stream().filter(x -> caseUUID.equals(x.getUuid())).findAny().orElse(null);
        assertThat(result1.getUuid()).isEqualTo(caseUUID);
        assertThat(result1.getReference()).isEqualTo(caseResponseEntity.getBody().getReference());

        CaseData result2 = responseEntityAsList.stream().filter(x -> caseUUID2.equals(x.getUuid())).findAny().orElse(null);
        assertThat(result2.getUuid()).isEqualTo(caseUUID2);
        assertThat(result2.getReference()).isEqualTo(caseResponseEntity2.getBody().getReference());
    }

    @Test
    public void shouldReturnOneCasesWhenUsingLastName() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> body = buildSearchBodyWithParameters("last-name", "Smith", null);
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
        ResponseEntity<Set<CaseData>> responseEntity = getResponseEntityFromSearchRequest(httpEntity);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody().size()).isEqualTo(1);
        assertThat(responseEntity.getBody().iterator().next().getUuid()).isEqualTo(caseUUID);
        assertThat(responseEntity.getBody().iterator().next().getReference()).isEqualTo(caseResponseEntity.getBody().getReference());
        assertThat(responseEntity.getBody().iterator().next().getType()).isEqualTo(CaseType.MIN.toString());
    }

    @Test
    public void shouldReturnOneCasesWhenUsingDateOfBirth() {
        HttpHeaders requestHeaders = buildHttpHeaders();
        Map<String, Object> body = buildSearchBodyWithParameters("date-of-birth", "1970-01-01", null);
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
        ResponseEntity<Set<CaseData>> responseEntity = getResponseEntityFromSearchRequest(httpEntity);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody().size()).isEqualTo(1);
        assertThat(responseEntity.getBody().iterator().next().getUuid()).isEqualTo(caseUUID);
        assertThat(responseEntity.getBody().iterator().next().getReference()).isEqualTo(caseResponseEntity.getBody().getReference());
        assertThat(responseEntity.getBody().iterator().next().getType()).isEqualTo(CaseType.MIN.toString());
    }

    @After
    public void tearDown() {
        clearDatabase();
    }

    private Map<String, Object> buildSearchBodyWithParameters(String key, String value, String ref) {
        Map<String, String> caseData = new HashMap<>();
        caseData.put(key, value);
        Map<String, Object> body = new HashMap<>();
        body.put("caseReference", ref);
        body.put("caseData", caseData);

        return body;
    }

    private Map<String, Object> buildSearchBodyWithAllParameters() {
        Map<String, String> caseData = new HashMap<>();
        caseData.put("first-name", "John");
        caseData.put("last-name", "Smith");
        caseData.put("date-of-birth", "1970-01-01");
        Map<String, Object> body = new HashMap<>();
        body.put("caseReference", "");
        body.put("caseData", caseData);

        return body;
    }

    private HttpHeaders buildHttpHeaders() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        requestHeaders.set("X-Auth-Userid", "1");
        requestHeaders.set("X-Auth-Username", "billy");
        requestHeaders.set("x-correlation-id", "12");
        return requestHeaders;
    }

    private Map<String, Object> buildCreateStageBody(String firstName, String lastName, LocalDate dob) {
        Map<String, String> stageData = new HashMap<>();
        stageData.put("first-name", firstName);
        stageData.put("last-name", lastName);
        stageData.put("date-of-birth", dob.toString());
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

    private ResponseEntity<Set<CaseData>> getResponseEntityFromSearchRequest(HttpEntity<?> httpEntity) {
        ParameterizedTypeReference<Set<CaseData>> myBean =
                new ParameterizedTypeReference<Set<CaseData>>() {
                };
        return restTemplate.exchange(
                "/search",
                HttpMethod.POST,
                httpEntity,
                myBean);
    }

    private void clearDatabase() {
        stageDataRepository.deleteAll();
        documentRepository.deleteAll();
        caseDataRepository.deleteAll();
    }

}
