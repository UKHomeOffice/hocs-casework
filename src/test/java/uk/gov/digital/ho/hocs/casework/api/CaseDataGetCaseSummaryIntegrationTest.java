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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseSummaryResponse;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditListResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.PermissionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:summary/beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:summary/afterTest.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
@ActiveProfiles("local")
public class CaseDataGetCaseSummaryIntegrationTest {
    private MockRestServiceServer mockInfoService;

    TestRestTemplate testRestTemplate = new TestRestTemplate();

    @LocalServerPort
    int port;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    ObjectMapper mapper;

    private final UUID CASE_UUID = UUID.fromString("14915b78-6977-42db-b343-0915a7f412a1");

    private final UUID INVALID_CASE_UUID = UUID.fromString("89334528-7769-2db4-b432-456091f132a1");

    private static final CaseDataType CASE_DATA_TYPE = CaseDataTypeFactory.from("TEST", "a1");

    private static final UUID EXTENSION_CASE_TYPE_ACTION_ID = UUID.fromString("a68b0ff2-a9fc-4312-8b28-504523d04026");
    private static final UUID APPEAL_CASE_TYPE_ACTION_ID = UUID.fromString("326eddb3-ba64-4253-ad39-916ccbb59f4e");


    private final Map<String, Object> somuType = Map.of(
            "uuid", "b124e71a-37be-410a-87a0-737be996d07e",
            "caseType", "CaseType",
            "type", "SomuType1",
            "active", true,
            "schema", "{\"showInSummary\": true}");

    private static final CaseTypeActionDto MOCK_CASE_TYPE_ACTION_EXTENSION_DTO = new CaseTypeActionDto(
            EXTENSION_CASE_TYPE_ACTION_ID,
            UUID.randomUUID(),
            "CASE_TYPE",
            "EXTENSION",
            "TEST_EXTENSION",
            "PIT Extension",
            1,
            10,
            true,
            "{}"
    );

    private static final CaseTypeActionDto MOCK_CASE_TYPE_ACTION_APPEAL_DTO = new CaseTypeActionDto(
            APPEAL_CASE_TYPE_ACTION_ID,
            UUID.randomUUID(),
            "CASE_TYPE",
            "APPEAL",
            "TEST_APPEAL",
            "APPEAL 1",
            1,
            10,
            true,
            "{}"
    );

    @Before
    public void setup() throws IOException {
        mockInfoService = buildMockService(restTemplate);
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new HashSet<>()), MediaType.APPLICATION_JSON));
        mockInfoService
                .expect(requestTo("http://localhost:8085/schema/caseType/TEST/summary"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new HashSet<>()), MediaType.APPLICATION_JSON));
        mockInfoService
                .expect(requestTo("http://localhost:8085/stages/caseType/TEST"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new HashSet<>()), MediaType.APPLICATION_JSON));
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/TEST/exemptionDates"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(Set.of()), MediaType.APPLICATION_JSON));
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new HashSet<>()), MediaType.APPLICATION_JSON));
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        mockInfoService
                .expect(manyTimes(),requestTo("http://localhost:8085/caseType/TEST/actions"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(List.of(MOCK_CASE_TYPE_ACTION_EXTENSION_DTO,MOCK_CASE_TYPE_ACTION_APPEAL_DTO)), MediaType.APPLICATION_JSON));
    }

    private MockRestServiceServer buildMockService(RestTemplate restTemplate) {
        MockRestServiceServer.MockRestServiceServerBuilder infoBuilder = bindTo(restTemplate);
        infoBuilder.ignoreExpectOrder(true);
        return infoBuilder.build();
    }

    @Test
    public void shouldReturnCaseSummaryWithActionData() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel());
        ResponseEntity<GetCaseSummaryResponse> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID + "/summary", GET,
                new HttpEntity<>(createValidAuthHeaders()),
                GetCaseSummaryResponse.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getActions()).isNotNull();
        assertThat(result.getBody().getActions().getCaseActionData().containsKey("appeals")).isTrue();
        assertThat(result.getBody().getActions().getCaseActionData().containsKey("extensions")).isTrue();

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
                .expect(requestTo("http://localhost:8087/audit/case/" + caseUUID + "?types=STAGE_ALLOCATED_TO_TEAM,STAGE_CREATED"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(restResponse), MediaType.APPLICATION_JSON));
    }
}
