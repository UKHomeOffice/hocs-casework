package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:case/beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:case/afterTest.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
public class CaseDataDeleteCaseIntegrationTest {
    private MockRestServiceServer mockInfoService;

    TestRestTemplate testRestTemplate = new TestRestTemplate();

    @LocalServerPort
    int port;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CaseDataRepository caseDataRepository;

    ObjectMapper mapper = new ObjectMapper();

    private final CaseDataType CASE_DATA_TYPE = new CaseDataType("TEST", "a1");

    private final UUID CASE_UUID = UUID.fromString("14915b78-6977-42db-b343-0915a7f412a1");

    private final UUID INVALID_CASE_UUID = UUID.fromString("89334528-7769-2db4-b432-456091f132a1");

    @Before
    public void setup() throws IOException {
        mockInfoService = buildMockService(restTemplate);
    }

    private MockRestServiceServer buildMockService(RestTemplate restTemplate) {
        MockRestServiceServer.MockRestServiceServerBuilder infoBuilder = bindTo(restTemplate);
        infoBuilder.ignoreExpectOrder(true);
        return infoBuilder.build();
    }

    @Test
    public void shouldDeleteValidCaseWithPermissionLevelOwner() throws JsonProcessingException {
        CaseData originalCaseData = caseDataRepository.findByUuid(CASE_UUID);
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "OWNER")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        CaseData postDeleteCaseData = caseDataRepository.findByUuid(CASE_UUID);
        assertThat(originalCaseData).isNotNull();
        assertThat(postDeleteCaseData).isNull();
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteCaseWithPermissionLevelWrite() throws JsonProcessingException {
        CaseData originalCaseData = caseDataRepository.findByUuid(CASE_UUID);
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "WRITE")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        CaseData postDeleteCaseData = caseDataRepository.findByUuid(CASE_UUID);
        assertThat(originalCaseData).isNotNull();
        assertThat(postDeleteCaseData).isNotNull();
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteCaseWithPermissionLevelRead() throws JsonProcessingException {
        CaseData originalCaseData = caseDataRepository.findByUuid(CASE_UUID);
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "READ")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        CaseData postDeleteCaseData = caseDataRepository.findByUuid(CASE_UUID);
        assertThat(originalCaseData).isNotNull();
        assertThat(postDeleteCaseData).isNotNull();
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteCaseWithPermissionLevelSummary() throws JsonProcessingException {
        CaseData originalCaseData = caseDataRepository.findByUuid(CASE_UUID);
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "SUMMARY")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        CaseData postDeleteCaseData = caseDataRepository.findByUuid(CASE_UUID);
        assertThat(originalCaseData).isNotNull();
        assertThat(postDeleteCaseData).isNotNull();
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteCaseWithPermissionLevelUnset() throws JsonProcessingException {
        CaseData originalCaseData = caseDataRepository.findByUuid(CASE_UUID);
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "UNSET")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        CaseData postDeleteCaseData = caseDataRepository.findByUuid(CASE_UUID);
        assertThat(originalCaseData).isNotNull();
        assertThat(postDeleteCaseData).isNotNull();
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteCaseWithPermissionLevelEmpty() throws JsonProcessingException {
        CaseData originalCaseData = caseDataRepository.findByUuid(CASE_UUID);
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        CaseData postDeleteCaseData = caseDataRepository.findByUuid(CASE_UUID);
        assertThat(originalCaseData).isNotNull();
        assertThat(postDeleteCaseData).isNotNull();
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteCaseWithPermissionLevelInvalid() throws JsonProcessingException {
        CaseData originalCaseData = caseDataRepository.findByUuid(CASE_UUID);
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "WRONG")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        CaseData postDeleteCaseData = caseDataRepository.findByUuid(CASE_UUID);
        assertThat(originalCaseData).isNotNull();
        assertThat(postDeleteCaseData).isNotNull();
    }

    @Test
    public void shouldReturnNotFoundWhenDeleteInvalidCaseWithPermissionLevelOwner() throws JsonProcessingException {
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "OWNER")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteInvalidCaseWithPermissionLevelWrite() throws JsonProcessingException {
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "WRITE")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteInvalidCaseWithPermissionLevelRead() throws JsonProcessingException {
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "READ")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteInvalidCaseWithPermissionLevelSummary() throws JsonProcessingException {
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "SUMMARY")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteInvalidCaseWithPermissionLevelUnset() throws JsonProcessingException {
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "UNSET")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteInvalidCaseWithPermissionLevelInvalid() throws JsonProcessingException {
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "WRONG")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteInvalidCaseWithPermissionLevelEmpty() throws JsonProcessingException {
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteInvalidCaseWithPermissionLevelNull() throws JsonProcessingException {
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", null)), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldDeleteValidCaseAndThenReturnNotFoundWhenDeleteSameCaseAgainWithPermissionLevelOwner() throws JsonProcessingException {
        CaseData originalCaseData = caseDataRepository.findByUuid(CASE_UUID);
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        ResponseEntity<String> result1 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "OWNER")), String.class);
        ResponseEntity<String> result2 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "OWNER")), String.class);
        CaseData postDeleteCaseData = caseDataRepository.findByUuid(CASE_UUID);
        assertThat(originalCaseData).isNotNull();
        assertThat(postDeleteCaseData).isNull();
        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String getBasePath() {
        return "http://localhost:" + port;
    }

    private HttpHeaders createValidAuthHeaders(String caseTypePermission, String permissionLevel) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Auth-Groups", "/UNIT1/44444444-2222-2222-2222-222222222222/" + caseTypePermission + "/" + permissionLevel);
        headers.add("X-Auth-Userid", "simon.mitchell@digital.homeoffice.gov.uk");
        headers.add("X-Correlation-Id", "1");
        return headers;
    }

    private String createBody(String caseType) {
        String body = "{\n" +
                "  \"type\":{\"displayCode\":\"" + caseType + "\",\"shortCode\":\"a1\"},\n" +
                "  \"data\": {\"DateReceived\":\"2018-01-01\"},\n" +
                "  \"received\":\"2018-01-01\",\n" +
                "  \"deadline\":\"2018-01-01\"\n" +
                "}";
        return body;
    }

    private String createBodyData(String caseType, String data) {
        String body = "{\n" +
                "  \"type\":{\"displayCode\":\"" + caseType + "\",\"shortCode\":\"a1\"},\n" +
                "  \"data\": " + data + ",\n" +
                "  \"received\":\"2018-01-01\",\n" +
                "  \"deadline\":\"2018-01-01\"\n" +
                "}";
        return body;
    }
}