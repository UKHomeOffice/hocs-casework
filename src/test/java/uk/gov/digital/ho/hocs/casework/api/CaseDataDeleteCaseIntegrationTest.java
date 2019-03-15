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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.casework.client.auditclient.EventType;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.AuditPayload;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditListResponse;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.PermissionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_ALLOCATED_TO_TEAM;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:case/beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:case/afterTest.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CaseDataDeleteCaseIntegrationTest {
    private MockRestServiceServer mockInfoService;

    TestRestTemplate testRestTemplate = new TestRestTemplate();

    @LocalServerPort
    int port;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CaseDataRepository caseDataRepository;

    @Autowired
    ObjectMapper mapper;

    private final CaseDataType CASE_DATA_TYPE = new CaseDataType("TEST", "a1");

    private final UUID CASE_UUID = UUID.fromString("14915b78-6977-42db-b343-0915a7f412a1");

    private final UUID INVALID_CASE_UUID = UUID.fromString("89334528-7769-2db4-b432-456091f132a1");


    @Before
    public void setup() throws IOException {
        mockInfoService = buildMockService(restTemplate);

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
    public void shouldDeleteValidCaseWithPermissionLevelOwner() throws JsonProcessingException {

        CaseData originalCaseData = caseDataRepository.findByUuid(CASE_UUID);
        setupMockTeams("TEST", 5);

        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders()), String.class);

        CaseData postDeleteCaseData = caseDataRepository.findByUuid(CASE_UUID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(originalCaseData).isNotNull();
        assertThat(postDeleteCaseData).isNull();
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteCaseWithPermissionLevelWrite() throws JsonProcessingException {

        CaseData originalCaseData = caseDataRepository.findByUuid(CASE_UUID);
        setupMockTeams("TEST", 3);
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders()), String.class);

        CaseData postDeleteCaseData = caseDataRepository.findByUuid(CASE_UUID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(originalCaseData).isNotNull();
        assertThat(postDeleteCaseData).isNotNull();
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteCaseWithPermissionLevelRead() throws JsonProcessingException {

        CaseData originalCaseData = caseDataRepository.findByUuid(CASE_UUID);
        setupMockTeams("TEST", 2);
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders()), String.class);

        CaseData postDeleteCaseData = caseDataRepository.findByUuid(CASE_UUID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(originalCaseData).isNotNull();
        assertThat(postDeleteCaseData).isNotNull();
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteCaseWithPermissionLevelSummary() throws JsonProcessingException {

        CaseData originalCaseData = caseDataRepository.findByUuid(CASE_UUID);
        setupMockTeams("TEST", 1);
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders()), String.class);

        CaseData postDeleteCaseData = caseDataRepository.findByUuid(CASE_UUID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(originalCaseData).isNotNull();
        assertThat(postDeleteCaseData).isNotNull();
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteCaseWithPermissionLevelUnset() throws JsonProcessingException {
        setupEmptyMockAudit(CASE_UUID);
        CaseData originalCaseData = caseDataRepository.findByUuid(CASE_UUID);
        setupMockTeams("TEST", 0);
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders()), String.class);

        CaseData postDeleteCaseData = caseDataRepository.findByUuid(CASE_UUID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(originalCaseData).isNotNull();
        assertThat(postDeleteCaseData).isNotNull();
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteCaseWithPermissionLevelEmpty() throws JsonProcessingException {
        setupEmptyMockAudit(CASE_UUID);
        CaseData originalCaseData = caseDataRepository.findByUuid(CASE_UUID);
        setupMockTeams("TEST1", 5);
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders()), String.class);

        CaseData postDeleteCaseData = caseDataRepository.findByUuid(CASE_UUID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(originalCaseData).isNotNull();
        assertThat(postDeleteCaseData).isNotNull();
    }


    @Test
    public void shouldReturnNotFoundWhenDeleteInvalidCaseWithPermissionLevelOwner() throws JsonProcessingException {
        setupMockTeams("TEST", 5);
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders()), String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteInvalidCaseWithPermissionLevelWrite() throws JsonProcessingException {
        setupMockTeams("TEST", 3);
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders()), String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteInvalidCaseWithPermissionLevelRead() throws JsonProcessingException {
        setupMockTeams("TEST", 2);
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders()), String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteInvalidCaseWithPermissionLevelSummary() throws JsonProcessingException {
        setupMockTeams("TEST", 1);
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders()), String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteInvalidCaseWithPermissionLevelUnset() throws JsonProcessingException {
        setupEmptyMockAudit(INVALID_CASE_UUID);
        setupMockTeams("TEST", 0);
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders()), String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteInvalidCaseWithPermissionLevelEmpty() throws JsonProcessingException {
        setupEmptyMockAudit(INVALID_CASE_UUID);
        setupMockTeams("TEST1", 0);
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders()), String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenDeleteInvalidCaseWithPermissionLevelNull() throws JsonProcessingException {
        setupEmptyMockAudit(INVALID_CASE_UUID);
        setupMockTeams("TEST1", 5);
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders()), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldDeleteValidCaseAndThenReturnNotFoundWhenDeleteSameCaseAgainWithPermissionLevelOwner() throws JsonProcessingException {

        CaseData originalCaseData = caseDataRepository.findByUuid(CASE_UUID);
        setupMockTeams("TEST", 5);
        setupMockTeams("TEST", 5);
        ResponseEntity<String> result1 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders()), String.class);
        ResponseEntity<String> result2 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID, DELETE, new HttpEntity(createValidAuthHeaders()), String.class);

        CaseData postDeleteCaseData = caseDataRepository.findByUuid(CASE_UUID);

        assertThat(originalCaseData).isNotNull();
        assertThat(postDeleteCaseData).isNull();
        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String getBasePath() {
        return "http://localhost:" + port;
    }

    private HttpHeaders createValidAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Auth-Groups", "/RERERCIiIiIiIiIiIiIiIg");
        headers.add("X-Auth-Userid", "a.person@digital.homeoffice.gov.uk");
        headers.add("X-Correlation-Id", "1");
        return headers;
    }

    private void setupMockTeams(String caseType, int permission) throws JsonProcessingException {
        Set<TeamDto> teamDtos = new HashSet<>();
        Set<PermissionDto> permissionDtos = new HashSet<>();
        permissionDtos.add(new PermissionDto(caseType, AccessLevel.from(permission)));
        TeamDto teamDto = new TeamDto("TEAM 1", UUID.fromString("44444444-2222-2222-2222-222222222222"), true, permissionDtos);
        teamDtos.add(teamDto);

        mockInfoService
                .expect(requestTo("http://localhost:8085/team"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(teamDtos), MediaType.APPLICATION_JSON));
    }

    private void setupEmptyMockAudit(UUID caseUUID) throws JsonProcessingException {
        GetAuditListResponse restResponse = new GetAuditListResponse(new HashSet<>());

        mockInfoService
                .expect(requestTo("http://localhost:8087/audit/case/" + caseUUID + "?types=STAGE_ALLOCATED_TO_TEAM"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(restResponse), MediaType.APPLICATION_JSON));
    }
}