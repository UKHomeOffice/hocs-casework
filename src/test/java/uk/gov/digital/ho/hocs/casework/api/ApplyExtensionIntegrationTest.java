package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
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
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseReferenceResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.PermissionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDeadlineExtension;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDeadlineExtensionType;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.*;
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
public class ApplyExtensionIntegrationTest {

    private MockRestServiceServer mockInfoService;
    private TestRestTemplate testRestTemplate = new TestRestTemplate();

    @LocalServerPort
    int port;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    CaseDataService caseDataService;

    @Autowired
    CaseNoteService caseNoteService;

    private ObjectMapper mapper = new ObjectMapper();

    private final UUID CASE_UUID = UUID.fromString("14915b78-6977-42db-b343-0915a7f412a1");
    private final UUID STAGE_UUID = UUID.fromString("e9151b83-7602-4419-be83-bff1c924c80d");

    private static final CaseDataType CASE_DATA_TYPE = new CaseDataType("TEST", "a1", "type", "previousCaseType");

    @Before
    public void setup() throws IOException {
        mockInfoService = buildMockService(restTemplate);

        mockInfoService
                .expect(ExpectedCount.times(3), requestTo("http://localhost:8085/caseType/shortCode/a1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CASE_DATA_TYPE), MediaType.APPLICATION_JSON));
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/TEST/deadline?received=2018-01-01&days=0&extensionDays=20"))
                .andExpect(method(GET))
                .andRespond(withSuccess("\"2018-01-29\"", MediaType.APPLICATION_JSON));
        mockInfoService
                .expect(requestTo("http://localhost:8085/stageType/INITIAL_DRAFT/deadline?received=2018-01-01&caseDeadline=2018-01-29"))
                .andExpect(method(GET))
                .andRespond(withSuccess("\"2018-01-29\"", MediaType.APPLICATION_JSON));
        mockInfoService
                .expect(requestTo("http://localhost:8085/stageType/INITIAL_DRAFT/deadlineWarning?received=2018-01-01&caseDeadlineWarning=2018-01-27"))
                .andExpect(method(GET))
                .andRespond(withSuccess("\"2018-01-27\"", MediaType.APPLICATION_JSON));
    }

    private MockRestServiceServer buildMockService(RestTemplate restTemplate) {
        MockRestServiceServer.MockRestServiceServerBuilder infoBuilder = bindTo(restTemplate);
        infoBuilder.ignoreExpectOrder(true);
        return infoBuilder.build();
    }

    @Test
    @Ignore
    public void shouldReturnOKWhenAllocationApplied() throws JsonProcessingException {
        setupMockTeams("TEST", 5, 2);

        final ResponseEntity<GetCaseReferenceResponse> result = testRestTemplate.exchange(
                getBasePath() + "/case/" + CASE_UUID + "/stage/" + STAGE_UUID + "/extension",
                POST, new HttpEntity(createBodyApplyExtension(), createValidAuthHeaders()), GetCaseReferenceResponse.class);

//        final CaseDeadlineExtension caseDeadlineExtension =
//                caseDataService.getCase(CASE_UUID).getDeadlineExtensions().stream().findAny().orElseThrow();

//        assertThat(caseDeadlineExtension.getCaseDeadlineExtensionType().getType()).isEqualTo("TEST_EXTENSION");
//        assertThat(caseDeadlineExtension.getCaseDeadlineExtensionType().getWorkingDays()).isEqualTo(20);

        final Set<CaseNote> caseNotes = caseNoteService.getCaseNotes(CASE_UUID);
        final CaseNote caseNote = caseNotes.stream().findFirst().get();

        assertThat(caseNote.getText()).isEqualTo("a test case note");

        assertThat(result.getBody().getReference()).isEqualTo("TEST/9990190/18");
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
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

    private void setupMockTeams(String caseType, int permission, int noOfTimesCalled) throws JsonProcessingException {
        Set<TeamDto> teamDtos = new HashSet<>();
        Set<PermissionDto> permissionDtos = new HashSet<>();
        permissionDtos.add(new PermissionDto(caseType, AccessLevel.from(permission)));
        TeamDto teamDto = new TeamDto("TEAM 1", UUID.fromString("44444444-2222-2222-2222-222222222222"), true, permissionDtos);
        teamDtos.add(teamDto);

        mockInfoService
                .expect(ExpectedCount.times(noOfTimesCalled), requestTo("http://localhost:8085/team"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(teamDtos), MediaType.APPLICATION_JSON));
    }

    private String createBodyApplyExtension() {
        return "{\n" +
                " \"type\" :\"TEST_EXTENSION\"\n," +
                " \"caseNote\" :\"a test case note\"\n" +
                "}";
    }
}