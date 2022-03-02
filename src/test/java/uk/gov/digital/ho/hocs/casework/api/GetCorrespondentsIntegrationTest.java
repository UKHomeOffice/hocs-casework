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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCorrespondentTypeResponse;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditListResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.PermissionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;

import java.io.IOException;
import java.util.Collections;
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

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:case/beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:case/afterTest.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
@ActiveProfiles({ "local", "integration" })
public class GetCorrespondentsIntegrationTest {

    private final String EMPTY_CORRESPONDENTS_EXPECTED_RESULT = "{\n " +
            " \"correspondents\" : [ ]\n" +
            "}";

    private TestRestTemplate testRestTemplate = new TestRestTemplate();

    @LocalServerPort
    int port;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    CorrespondentRepository correspondentRepository;

    private ObjectMapper mapper = new ObjectMapper();

    private final UUID CASE_UUID = UUID.fromString("14915b78-6977-42db-b343-0915a7f412a1");
    private final UUID STAGE_UUID = UUID.fromString("e9151b83-7602-4419-be83-bff1c924c80d");
    private final UUID CORRESPONDENT_UUID = UUID.fromString("2c9e1eb9-ee78-4f57-a626-b8b75cf3b937");

    private final UUID INVALID_CASE_UUID = UUID.fromString("89334528-7769-2db4-b432-456091f132a1");

    private static final CaseDataType CASE_DATA_TYPE = CaseDataTypeFactory.from("TEST", "a1");
    private MockRestServiceServer mockInfoService;

    @Before
    public void setup() throws IOException {
        mockInfoService = buildMockService(restTemplate);
        mockInfoService
                .expect(ExpectedCount.times(2), requestTo("http://localhost:8085/caseType"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new HashSet<>()), MediaType.APPLICATION_JSON));
        mockInfoService
                .expect(ExpectedCount.times(3), requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        mockInfoService
                .expect(requestTo("http://localhost:8085/correspondentType/TEST"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(GetCorrespondentTypeResponse.from(Collections.emptySet())), MediaType.APPLICATION_JSON));
    }

    private MockRestServiceServer buildMockService(RestTemplate restTemplate) {
        MockRestServiceServer.MockRestServiceServerBuilder infoBuilder = bindTo(restTemplate);
        infoBuilder.ignoreExpectOrder(true);
        return infoBuilder.build();
    }

    @Test
    public void shouldReturnCorrespondentsForValidCaseWithPermissionLevelOwner() throws JsonProcessingException {
        AccessLevel permissionLevel = AccessLevel.OWNER;
        setupMockTeams("TEST", permissionLevel, 1);

        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID + "/correspondent", GET, new HttpEntity(createValidAuthHeaders()), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnCorrespondentsForValidCaseWithPermissionLevelCaseAdmin() throws JsonProcessingException {
        AccessLevel permissionLevel = AccessLevel.CASE_ADMIN;
        setupMockTeams("TEST", permissionLevel, 1);

        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID + "/correspondent", GET, new HttpEntity(createValidAuthHeaders()), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnCorrespondentsForValidCaseWithPermissionLevelWrite() throws JsonProcessingException {
        AccessLevel permissionLevel = AccessLevel.WRITE;
        setupMockTeams("TEST", permissionLevel, 1);

        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID + "/correspondent", GET, new HttpEntity(createValidAuthHeaders()), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnCorrespondentsForValidCaseWithPermissionLevelRead() throws JsonProcessingException {
        AccessLevel permissionLevel = AccessLevel.READ;
        setupMockTeams("TEST", permissionLevel, 1);

        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID + "/correspondent", GET, new HttpEntity(createValidAuthHeaders()), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnUnauthorisedWhenGetCorrespondentsForValidCaseWithPermissionLevelSummary() throws JsonProcessingException {
        AccessLevel permissionLevel = AccessLevel.SUMMARY;
        setupMockTeams("TEST", permissionLevel, 1);

        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID + "/correspondent", GET, new HttpEntity(createValidAuthHeaders()), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenGetCorrespondentsForValidCaseWithPermissionLevelMigrate() throws JsonProcessingException {
        AccessLevel permissionLevel = AccessLevel.MIGRATE;
        setupMockTeams("TEST", permissionLevel, 1);

        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID + "/correspondent", GET, new HttpEntity(createValidAuthHeaders()), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenGetCorrespondentsForValidCaseWithPermissionLevelUnset() throws JsonProcessingException {
        setupEmptyMockAudit(CASE_UUID);
        AccessLevel permissionLevel = AccessLevel.UNSET;
        setupMockTeams("TEST", permissionLevel, 1);

        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID + "/correspondent", GET, new HttpEntity(createValidAuthHeaders()), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorisedWhenGetCorrespondentsForValidCaseWithoutPermission() throws JsonProcessingException {
        setupEmptyMockAudit(CASE_UUID);
        AccessLevel permissionLevel = AccessLevel.OWNER;
        setupMockTeams("TEST", permissionLevel, 1);

        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID + "/correspondent", GET, new HttpEntity(createValidAuthHeaders()), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnOkAndEmptySetSetWhenGetCorrespondentsForInvalidCaseWithPermissionLevelOwner() throws JsonProcessingException {
        AccessLevel permissionLevel = AccessLevel.OWNER;
        setupMockTeams("TEST", permissionLevel, 1);

        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID + "/correspondent", GET, new HttpEntity(createValidAuthHeaders()), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(EMPTY_CORRESPONDENTS_EXPECTED_RESULT);
    }

    @Test
    public void shouldReturnOkAndEmptySetWhenGetCorrespondentsForInvalidCaseWithPermissionLevelWrite() throws JsonProcessingException {
        AccessLevel permissionLevel = AccessLevel.WRITE;
        setupMockTeams("TEST", permissionLevel, 1);

        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID + "/correspondent", GET, new HttpEntity(createValidAuthHeaders()), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(EMPTY_CORRESPONDENTS_EXPECTED_RESULT);
    }

    @Test
    public void shouldReturnOkAndEmptySetWhenGetCorrespondentsForInvalidCaseWithPermissionLevelRead() throws JsonProcessingException {
        AccessLevel permissionLevel = AccessLevel.READ;
        setupMockTeams("TEST", permissionLevel, 1);

        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID + "/correspondent", GET, new HttpEntity(createValidAuthHeaders()), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(EMPTY_CORRESPONDENTS_EXPECTED_RESULT);

    }

    @Test
    public void shouldReturnUnauthorisedWhenGetCorrespondentsForInvalidCaseWithPermissionLevelSummary() throws JsonProcessingException {
        AccessLevel permissionLevel = AccessLevel.SUMMARY;
        setupMockTeams("TEST", permissionLevel, 1);

        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID + "/correspondent", GET, new HttpEntity(createValidAuthHeaders()), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    }

    @Test
    public void shouldReturnUnauthorisedWhenGetCorrespondentsForInvalidCaseWithPermissionLevelMigrate() throws JsonProcessingException {
        AccessLevel permissionLevel = AccessLevel.SUMMARY;
        setupMockTeams("TEST", permissionLevel, 1);

        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID + "/correspondent", GET, new HttpEntity(createValidAuthHeaders()), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    }

    @Test
    public void shouldReturnUnauthorisedWhenGetCorrespondentsForInvalidCaseWithPermissionLevelUnset() throws JsonProcessingException {
        setupEmptyMockAudit(INVALID_CASE_UUID);
        AccessLevel permissionLevel = AccessLevel.UNSET;
        setupMockTeams("TEST", permissionLevel, 1);

        ResponseEntity<String> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + INVALID_CASE_UUID + "/correspondent", GET, new HttpEntity(createValidAuthHeaders()), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    @Test
    public void shouldReturnOkWhenGetCorrespondentAndThenOkAndEmptySetWhenCorrespondentsDeleted() throws JsonProcessingException {
        AccessLevel permissionLevel = AccessLevel.OWNER;
        setupMockTeams("TEST", permissionLevel, 4);

        mockInfoService
                .expect(ExpectedCount.times(2), requestTo("http://localhost:8085/correspondentType/TEST"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(GetCorrespondentTypeResponse.from(Collections.emptySet())), MediaType.APPLICATION_JSON));

        ResponseEntity<String> result1 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID + "/correspondent", GET, new HttpEntity(createValidAuthHeaders()), String.class);
        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.OK);
        ResponseEntity<String> result2 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID + "/stage/" + STAGE_UUID + "/correspondent/" + CORRESPONDENT_UUID, DELETE, new HttpEntity(createValidAuthHeaders()), String.class);
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.OK);
        ResponseEntity<String> result3 = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID + "/correspondent", GET, new HttpEntity(createValidAuthHeaders()), String.class);
        assertThat(result3.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result3.getBody()).isEqualTo(EMPTY_CORRESPONDENTS_EXPECTED_RESULT);
    }

    private String getBasePath() {
        return "http://localhost:" + port;
    }

    private HttpHeaders createValidAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Auth-Groups", "/RERERCIiIiIiIiIiIiIiIg");
        headers.add("X-Auth-Userid", "4035d37f-9c1d-436e-99de-1607866634d4");
        headers.add("X-Correlation-Id", "1");
        return headers;
    }

    private void setupMockTeams(String caseType, AccessLevel permission, int noOfCalls) throws JsonProcessingException {
        Set<TeamDto> teamDtos = new HashSet<>();
        Set<PermissionDto> permissionDtos = new HashSet<>();
        permissionDtos.add(new PermissionDto(caseType, permission));
        TeamDto teamDto = new TeamDto("TEAM 1", UUID.fromString("44444444-2222-2222-2222-222222222222"), true, permissionDtos);
        teamDtos.add(teamDto);

        mockInfoService
                .expect(ExpectedCount.times(noOfCalls), requestTo("http://localhost:8085/team"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(teamDtos), MediaType.APPLICATION_JSON));
    }

    private void setupEmptyMockAudit(UUID caseUUID) throws JsonProcessingException {
        GetAuditListResponse restResponse = new GetAuditListResponse(new HashSet<>());

        mockInfoService
                .expect(requestTo("http://localhost:8087/audit/case/" + caseUUID + "?types=STAGE_ALLOCATED_TO_TEAM,STAGE_CREATED"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(restResponse), MediaType.APPLICATION_JSON));
    }

}
