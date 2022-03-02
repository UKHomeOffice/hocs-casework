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
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.StageTypeDto;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.client.infoclient.PermissionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
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
@ActiveProfiles({ "local", "integration" })
public class CaseDataDeadlinesIntegrationTest {
    private MockRestServiceServer mockInfoService;
    private TestRestTemplate testRestTemplate = new TestRestTemplate();
    ObjectMapper mapper = new ObjectMapper();

    @LocalServerPort
    int port;

    @Autowired
    private CaseDataRepository caseDataRepository;

    @Autowired
    private StageRepository stageRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Before
    public void setup() {
        mockInfoService = buildMockService(restTemplate);
    }


    @Test
    public void shouldCreateACaseWithCorrectDeadlines() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel());

        ResponseEntity<CreateCaseResponse> result =
                getCreateCaseResponse(createBody("TEST"), "TEST", "5");

        CaseData caseData = caseDataRepository.findActiveByUuid(result.getBody().getUuid());

        assertThat(caseData).isNotNull();
        assertThat(caseData.getDateReceived()).isEqualTo("2021-12-08");
        assertThat(caseData.getCaseDeadlineWarning()).isEqualTo("2021-12-31");
        assertThat(caseData.getCaseDeadline()).isEqualTo("2022-01-10");
    }

    @Test
    public void shouldOverrideCaseAndStageSlas() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel());

        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/b2e0b71c-d9be-4a0b-8e6c-38d06146f0e0/stage/7c4fd5df-16d9-4304-9ba9-ed01e8c6b133/deadline",
                PUT, new HttpEntity(19, createValidAuthHeaders()), Void.class);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);

        final CaseData caseData =
                caseDataRepository.findActiveByUuid(UUID.fromString("b2e0b71c-d9be-4a0b-8e6c-38d06146f0e0"));

        assertThat(caseData).isNotNull();
        assertThat(caseData.getDateReceived()).isEqualTo("2021-12-08");
        assertThat(caseData.getCaseDeadline()).isEqualTo("2022-01-07");

        final Stage stage = stageRepository
                .findActiveBasicStageByCaseUuidStageUUID(
                        UUID.fromString("b2e0b71c-d9be-4a0b-8e6c-38d06146f0e0"),
                        UUID.fromString("7c4fd5df-16d9-4304-9ba9-ed01e8c6b133"));

        assertThat(stage.getDeadline()).isEqualTo("2021-12-22");
    }

    @Test
    public void shouldUpdateReceivedDate() throws JsonProcessingException {
        setupMockTeams("TEST", AccessLevel.OWNER.getLevel());

        ResponseEntity<Void> result = testRestTemplate.exchange(
                getBasePath() + "/case/b2e0b71c-d9be-4a0b-8e6c-38d06146f0e0/stage/7c4fd5df-16d9-4304-9ba9-ed01e8c6b133/dateReceived",
                PUT, new HttpEntity(LocalDate.parse("2021-12-10"), createValidAuthHeaders()), Void.class);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);

        final CaseData caseData =
                caseDataRepository.findActiveByUuid(UUID.fromString("b2e0b71c-d9be-4a0b-8e6c-38d06146f0e0"));

        assertThat(caseData).isNotNull();
        assertThat(caseData.getDateReceived()).isEqualTo("2021-12-10");
        assertThat(caseData.getCaseDeadline()).isEqualTo("2022-01-12");

        final Stage stage = stageRepository
                .findActiveBasicStageByCaseUuidStageUUID(
                        UUID.fromString("b2e0b71c-d9be-4a0b-8e6c-38d06146f0e0"),
                        UUID.fromString("7c4fd5df-16d9-4304-9ba9-ed01e8c6b133"));

        assertThat(stage.getDeadline()).isEqualTo("2021-12-24");
    }

    private ResponseEntity<CreateCaseResponse> getCreateCaseResponse(String body, String caseTypePermission, String permissionLevel) {
        return testRestTemplate.exchange(
                getBasePath() + "/case", POST, new HttpEntity(body, createValidAuthHeaders()), CreateCaseResponse.class);
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

        Set<String> exemptionDates = Set.of(
                "2020-01-01",
                "2020-04-10",
                "2020-04-13",
                "2020-05-08",
                "2020-05-25",
                "2020-08-31",
                "2020-12-25",
                "2020-12-28",
                "2021-01-01",
                "2021-04-02",
                "2021-04-05",
                "2021-05-03",
                "2021-05-31",
                "2021-08-30",
                "2021-12-27",
                "2021-12-28",
                "2022-01-03"
        );

        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/type/TEST"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CaseDataTypeFactory.from("TEST", "a1")), MediaType.APPLICATION_JSON));

        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/type/TEST"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(CaseDataTypeFactory.from("TEST", "a1")), MediaType.APPLICATION_JSON));

        mockInfoService
                .expect(requestTo("http://localhost:8085/team"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(teamDtos), MediaType.APPLICATION_JSON));

        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/TEST/exemptionDates"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(exemptionDates), MediaType.APPLICATION_JSON));

        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/TEST/exemptionDates"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(exemptionDates), MediaType.APPLICATION_JSON));

        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/TEST/exemptionDates"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(exemptionDates), MediaType.APPLICATION_JSON));

        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/TEST/exemptionDates"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(exemptionDates), MediaType.APPLICATION_JSON));

        mockInfoService
                .expect(requestTo("http://localhost:8085/stages/caseType/TEST"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(Set.of(
                        new StageTypeDto("initial draft", "DRAFT", "INITIAL_DRAFT", 10, 5, 1))),
                        MediaType.APPLICATION_JSON));
    }

    private MockRestServiceServer buildMockService(RestTemplate restTemplate) {
        MockRestServiceServer.MockRestServiceServerBuilder infoBuilder = bindTo(restTemplate);
        infoBuilder.ignoreExpectOrder(true);
        return infoBuilder.build();
    }

    private String createBody(String caseType) {
        return "{\n" +
                "  \"type\": \"" + caseType + "\",\n" +
                "  \"data\": {\"DateReceived\":\"2021-12-08\"},\n" +
                "  \"received\":\"2021-12-08\"\n" +
                "}";
    }
}
