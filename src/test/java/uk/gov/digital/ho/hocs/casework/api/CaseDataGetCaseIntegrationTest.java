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
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
@Sql(scripts = "classpath:beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:afterTest.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
public class CaseDataGetCaseIntegrationTest {
    private MockRestServiceServer mockInfoService;

    TestRestTemplate testRestTemplate = new TestRestTemplate();

    @LocalServerPort
    int port;

    @Autowired
    private RestTemplate restTemplate;

    ObjectMapper mapper = new ObjectMapper();

    private final UUID CASE_UUID = UUID.fromString("14915b78-6977-42db-b343-0915a7f412a1");

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
    public void shouldReturnCaseDetailsWhenGetValidCaseWithPermissionLevelOwner() throws JsonProcessingException {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "OWNER")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnCaseDetailsWhenGetValidCaseWithPermissionLevelWrite() throws JsonProcessingException {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "WRITE")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnCaseDetailsWhenGetValidCaseWithPermissionLevelRead() throws JsonProcessingException {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "READ")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnUnauthorisedWhenGetValidCaseWithPermissionLevelSummary() throws JsonProcessingException {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "SUMMARY")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenGetValidCaseWithPermissionLevelEmpty() throws JsonProcessingException {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenGetValidCaseWithInvalidPermission() throws JsonProcessingException {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "WRONG")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnNotFoundWhenGetInValidCaseWithPermissionLevelOwner() throws JsonProcessingException {
        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "OWNER")), Void.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnNotFoundWhenGetInValidCaseWithPermissionLevelWrite() throws JsonProcessingException {
        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "WRITE")), Void.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnNotFoundWhenGetInValidCaseWithPermissionLevelRead() throws JsonProcessingException {
        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "READ")), Void.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnUnauthorisedForInValidCaseWithPermissionLevelSummary() throws JsonProcessingException {
        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "SUMMARY")), Void.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedForInValidCaseWithPermissionLevelUnset() throws JsonProcessingException {
        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "UNSET")), Void.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedForInValidCaseWithPermissionLevelEmpty() throws JsonProcessingException {
        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "")), Void.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedForInValidCaseWithPermissionLevelInvalid() throws JsonProcessingException {
        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "WRONG")), Void.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnCaseWhenGetValidCaseWithValidPermissionsAndReturnUnauthorisedWithEmptyPermissionsForSameCase() throws JsonProcessingException {
        ResponseEntity<String> validPermissionResult = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "OWNER")), String.class);
        ResponseEntity<String> invalidPermissionResult = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "")), String.class);
        assertThat(validPermissionResult.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(invalidPermissionResult.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnCaseWhenGetValidCaseWithValidPermissionsAndReturnUnauthorisedWithInvalidPermissionsForSameCase() throws JsonProcessingException {
        ResponseEntity<String> validPermissionResult = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "OWNER")), String.class);
        ResponseEntity<String> invalidPermissionResult = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "FRED")), String.class);
        assertThat(validPermissionResult.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(invalidPermissionResult.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
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
}