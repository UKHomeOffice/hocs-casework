package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.CORBA.BAD_CONTEXT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseNoteRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseNoteResponse;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseNoteRepository;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:notes/beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:notes/afterTest.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CaseNoteIntegrationTest {
    private MockRestServiceServer mockInfoService;

    TestRestTemplate testRestTemplate = new TestRestTemplate();

    @LocalServerPort
    int port;

    @Autowired
    private CaseNoteRepository caseNoteRepository;

    @Autowired
    private RestTemplate restTemplate;

    ObjectMapper mapper = new ObjectMapper();

    private final UUID CASE_UUID = UUID.fromString("fbdbaeab-6719-4e3a-a221-d061dde469a1");

    private final UUID INVALID_CASE_UUID = UUID.fromString("89334528-7769-2db4-b432-456091f132a1");

    private static final CaseDataType CASE_DATA_TYPE = new CaseDataType("TEST", "a1");

    @Before
    public void setup() throws IOException {
        mockInfoService = buildMockService(restTemplate);
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
    }


    private MockRestServiceServer buildMockService(RestTemplate restTemplate) {
        MockRestServiceServer.MockRestServiceServerBuilder infoBuilder = bindTo(restTemplate);
        infoBuilder.ignoreExpectOrder(true);
        return infoBuilder.build();
    }

    @Test
    public void shouldReturnCaseNotesWhenGetValidCaseWithPermissionLevelOwner() {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID + "/note", GET, new HttpEntity(createValidAuthHeaders("TEST", "OWNER")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnCaseNoteWhenGetValidCaseNoteWithPermissionLevelOwner() {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID + "/note/a2bb3622-b38a-479d-b390-f633bf15f329", GET, new HttpEntity(createValidAuthHeaders("TEST", "OWNER")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnBadRequestAndNotCreateACaseNoteWhenNoRequestBody() {
        long numberOfCasesBefore = caseNoteRepository.count();
        ResponseEntity<Void> result = getCreateCaseNoteVoidResponse(null, "TEST", "OWNER");
        long numberOfCasesAfter = caseNoteRepository.count();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldReturnUnauthorisedAndNotCreateACaseWithPermissionLevelSummary() throws JsonProcessingException {
        long numberOfCasesBefore = caseNoteRepository.count();
        ResponseEntity<Void> result = getCreateCaseNoteVoidResponse(createBody(), "TEST", "SUMMARY");
        long numberOfCasesAfter = caseNoteRepository.count();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldReturnBadRequestAndNotCreateACaseWhenNoRequestBody() {
        long numberOfCasesBefore = caseNoteRepository.count();
        ResponseEntity<Void> result = getCreateCaseNoteVoidResponse(null, "TEST", "OWNER");
        long numberOfCasesAfter = caseNoteRepository.count();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    @Test
    public void shouldReturnBadRequestAndNotCreateACaseWhenCaseUUIDInvalid() throws JsonProcessingException {
        long numberOfCasesBefore = caseNoteRepository.count();
        ResponseEntity<Void> result = getCreateCaseNoteInavlidCaseUUIDResponse(createBody(), "TEST", "OWNER");
        long numberOfCasesAfter = caseNoteRepository.count();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore);
    }

    private String getBasePath() {
        return "http://localhost:" + port;
    }

    private HttpHeaders createValidAuthHeaders(String caseType, String permissionLevel) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Auth-Groups", "/UNIT1/44444444-2222-2222-2222-222222222222/" + caseType + "/" + permissionLevel);
        headers.add("X-Auth-Userid", "simon.mitchell@digital.homeoffice.gov.uk");
        headers.add("X-Correlation-Id", "1");
        return headers;
    }

    @Test
    public void shouldCreateACaseNoteWithPermissionLevelRead() throws JsonProcessingException {
        long numberOfCasesBefore = caseNoteRepository.count();
        ResponseEntity<CreateCaseNoteResponse> result = getCreateCaseNoteResponse(createBody(), "TEST","READ");
        CaseNote caseData = caseNoteRepository.findByUuid(result.getBody().getUuid());
        long numberOfCasesAfter = caseNoteRepository.count();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getUuid()).isNotNull();
        assertThat(caseData).isNotNull();
        assertThat(numberOfCasesAfter).isEqualTo(numberOfCasesBefore + 1l);
    }

    private ResponseEntity<CreateCaseNoteResponse> getCreateCaseNoteResponse(String body, String caseTypePermission, String permissionLevel) {
        return testRestTemplate.exchange(
                getBasePath() + "/case/"+ CASE_UUID +"/note", POST, new HttpEntity(body, createValidAuthHeaders(caseTypePermission, permissionLevel)), CreateCaseNoteResponse.class);
    }

    private ResponseEntity<Void> getCreateCaseNoteVoidResponse(String body, String caseTypePermission, String permissionLevel) {
        return testRestTemplate.exchange(
                getBasePath() + "/case/"+ CASE_UUID +"/note", POST, new HttpEntity(body, createValidAuthHeaders(caseTypePermission, permissionLevel)), Void.class);
    }

    private ResponseEntity<Void> getCreateCaseNoteInavlidCaseUUIDResponse(String body, String caseTypePermission, String permissionLevel) {
        return testRestTemplate.exchange(
                getBasePath() + "/case/"+ INVALID_CASE_UUID +"/note", POST, new HttpEntity(body, createValidAuthHeaders(caseTypePermission, permissionLevel)), Void.class);
    }

    private String createBody() throws JsonProcessingException {
        CreateCaseNoteRequest request = new CreateCaseNoteRequest("TEST", "Case Note Text");
        return mapper.writeValueAsString(request);
    }
}