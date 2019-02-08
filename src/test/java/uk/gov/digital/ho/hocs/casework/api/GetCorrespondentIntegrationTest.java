package uk.gov.digital.ho.hocs.casework.api;

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
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;
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
public class GetCorrespondentIntegrationTest {

    private TestRestTemplate testRestTemplate = new TestRestTemplate();

    @LocalServerPort
    int port;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    CorrespondentRepository correspondentRepository;

    private ObjectMapper mapper = new ObjectMapper();

    private final UUID CASE_UUID1 = UUID.fromString("14915b78-6977-42db-b343-0915a7f412a1");
    private final UUID STAGE_UUID_ALLOCATED_TO_USER = UUID.fromString("e9151b83-7602-4419-be83-bff1c924c80d");
    private final UUID INVALID_CASE_UUID = UUID.fromString("89334528-7769-2db4-b432-456091f132a1");
    private final UUID CORRESPONDENT_UUID = UUID.fromString("2c9e1eb9-ee78-4f57-a626-b8b75cf3b937");

    private static final CaseDataType CASE_DATA_TYPE = new CaseDataType("TEST", "a1");

    @Before
    public void setup() throws IOException {
        MockRestServiceServer mockInfoService = buildMockService(restTemplate);
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
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
    public void shouldReturnCorrespondentForValidCaseWithPermissionLevelOwner() {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/correspondent/" + CORRESPONDENT_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "5")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnCorrespondentsForValidCaseWithPermissionLevelWrite() {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/correspondent/" + CORRESPONDENT_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "3")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnCorrespondentsForValidCaseWithPermissionLevelRead() {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/correspondent/" + CORRESPONDENT_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "2")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnUnauthorisedWhenGetCorrespondentsForValidCaseWithPermissionLevelSummary() {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/correspondent/" + CORRESPONDENT_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "1")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenGetCorrespondentsForValidCaseWithPermissionLevelUnset() {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/correspondent/" + CORRESPONDENT_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "0")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenGetCorrespondentsForValidCaseWithPermissionLevelEmpty() {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/correspondent/" + CORRESPONDENT_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenGetCorrespondentsForValidCaseWithPermissionLevelNull() {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/correspondent/" + CORRESPONDENT_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", null)), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnNotFoundSetWhenGetCorrespondentsForInvalidCaseWithPermissionLevelOwner() {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID + "/correspondent/" + CORRESPONDENT_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "5")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnNotFoundWhenGetCorrespondentsForInvalidCaseWithPermissionLevelWrite() {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID + "/correspondent/" + CORRESPONDENT_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "3")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnNotFoundWhenGetCorrespondentsForInvalidCaseWithPermissionLevelRead() {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID + "/correspondent/" + CORRESPONDENT_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "2")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    public void shouldReturnUnauthorisedWhenGetCorrespondentsForInvalidCaseWithPermissionLevelSummary() {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID + "/correspondent/" + CORRESPONDENT_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "1")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    }

    @Test
    public void shouldReturnUnauthorisedWhenGetCorrespondentsForInvalidCaseWithPermissionLevelUnset() {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID + "/correspondent/" + CORRESPONDENT_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "0")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenGetCorrespondentsForInvalidCaseWithPermissionLevelEmpty() {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID + "/correspondent/" + CORRESPONDENT_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "")), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenGetCorrespondentsForInvalidCaseWithPermissionLevelNull() {
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID + "/correspondent/" + CORRESPONDENT_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", null)), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnOkWhenGetCorrespondentAndThenOkAndEmptySetWhenCorrespondentsDeleted() {
        ResponseEntity<String> result1 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/correspondent/" + CORRESPONDENT_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "5")), String.class);
        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.OK);
        ResponseEntity<String> result2 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/stage/" + STAGE_UUID_ALLOCATED_TO_USER + "/correspondent/" + CORRESPONDENT_UUID, DELETE, new HttpEntity(createValidAuthHeaders("TEST", "5")), String.class);
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.OK);
        ResponseEntity<String> result3 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID1 + "/correspondent/" + CORRESPONDENT_UUID, GET, new HttpEntity(createValidAuthHeaders("TEST", "5")), String.class);
        assertThat(result3.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String getBasePath() {
        return "http://localhost:" + port;
    }

    private HttpHeaders createValidAuthHeaders(String caseType, String permissionLevel) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Auth-Groups", "/RERERCIiIiIiIiIiIiIiIg/" + caseType + "/" + permissionLevel);
        headers.add("X-Auth-Userid", "4035d37f-9c1d-436e-99de-1607866634d4");
        headers.add("X-Correlation-Id", "1");
        return headers;
    }

}
