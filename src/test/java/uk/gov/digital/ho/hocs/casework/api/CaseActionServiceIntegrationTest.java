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
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDeadlineExtensionDto;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseReferenceResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeActionDto;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:action/beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:action/afterTest.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CaseActionServiceIntegrationTest {

    private MockRestServiceServer mockInfoService;

    private TestRestTemplate testRestTemplate = new TestRestTemplate();

    @Autowired
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @LocalServerPort
    int port;

    private static final UUID CASE_TYPE_ACTION_ID_EXTENSION_CREATE = UUID.randomUUID();
    private static final UUID CASE_TYPE_ACTION_ID_APPEAL = UUID.randomUUID();
    private static final UUID CASE_TYPE_ACTION_ID_NON_EXISTENT = UUID.randomUUID();

    private static final CaseTypeActionDto MOCK_CASE_TYPE_ACTION_EXTENSION_DTO = new CaseTypeActionDto(
            CASE_TYPE_ACTION_ID_EXTENSION_CREATE,
            UUID.randomUUID(),
            "CASE_TYPE",
            "EXTENSION",
            "PIT Extension",
            10,
            true,
            "{}"
    );

    private static final CaseTypeActionDto MOCK_CASE_TYPE_ACTION_APPEAL_DTO = new CaseTypeActionDto(
            CASE_TYPE_ACTION_ID_APPEAL,
            UUID.randomUUID(),
            "CASE_TYPE",
            "APPEAL",
            "APPEAL 1",
            10,
            true,
            "{}"
    );

    @Before
    public void setUp() throws JsonProcessingException {
        mockInfoService = buildMockService(restTemplate);
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/FOI/actions/" + CASE_TYPE_ACTION_ID_EXTENSION_CREATE))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(MOCK_CASE_TYPE_ACTION_EXTENSION_DTO), MediaType.APPLICATION_JSON));
        mockInfoService
                .expect(manyTimes(),requestTo("http://localhost:8085/caseType/FOI/actions/" + CASE_TYPE_ACTION_ID_NON_EXISTENT))
                .andExpect(method(GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        mockInfoService
                .expect(manyTimes(),requestTo("http://localhost:8085/caseType/FOI/actions/" + CASE_TYPE_ACTION_ID_APPEAL))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(MOCK_CASE_TYPE_ACTION_APPEAL_DTO), MediaType.APPLICATION_JSON));
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/FOI/deadline?received=" + LocalDate.now() + "&days=8"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(LocalDate.now().plusDays(8).toString()), MediaType.APPLICATION_JSON));
        mockInfoService
                .expect(requestTo("http://localhost:8085/caseType/FOI/deadlineWarning?received=" + LocalDate.now() + "&days=8"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(LocalDate.now().plusDays(6).toString()), MediaType.APPLICATION_JSON));
        mockInfoService
                .expect(requestTo("http://localhost:8085/stageType/INITIAL_DRAFT/deadline?received=2018-01-01&caseDeadline=" + LocalDate.now().plusDays(8)))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(LocalDate.now().plusDays(8).toString()), MediaType.APPLICATION_JSON));
        mockInfoService
                .expect(requestTo("http://localhost:8085/stageType/INITIAL_DRAFT/deadlineWarning?received=2018-01-01&caseDeadlineWarning=" + LocalDate.now().plusDays(6)))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(LocalDate.now().plusDays(6).toString()), MediaType.APPLICATION_JSON));
    }

    @Test
    public void shouldCreateDeadlineExtension() throws JsonProcessingException {

        UUID caseId = UUID.fromString("14915b78-6977-42db-b343-0915a7f412a1");
        UUID stageUUID = UUID.randomUUID();
        UUID caseTypeActionUuid = CASE_TYPE_ACTION_ID_EXTENSION_CREATE;
        String caseType = "FOI";
        String caseTypeActionLabel = "PIT Extension";

        ActionDataDto actionDataDto = new ActionDataDeadlineExtensionDto(null, caseTypeActionUuid, caseTypeActionLabel, "today", 8, "NOTE");

        String requestBody = mapper.writeValueAsString(actionDataDto);
        ResponseEntity<GetCaseReferenceResponse> response = testRestTemplate.exchange(
                getBasePath() + "/case/" + caseId + "/stage/" + stageUUID + "/caseType/" + caseType + "/action",
                POST,
                new HttpEntity(requestBody, createValidAuthHeaders()),
                GetCaseReferenceResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturn404whenNoActionWithID() throws JsonProcessingException {

        UUID caseId = UUID.fromString("14915b78-6977-42db-b343-0915a7f412a1");
        UUID stageUUID = UUID.randomUUID();
        UUID caseTypeActionUuid = CASE_TYPE_ACTION_ID_NON_EXISTENT;
        String caseType = "FOI";
        String caseTypeActionLabel = "PIT Extension";

        ActionDataDto actionDataDto = new ActionDataDeadlineExtensionDto(null, caseTypeActionUuid, caseTypeActionLabel, "today", 8, "NOTE");

        String requestBody = mapper.writeValueAsString(actionDataDto);
        ResponseEntity<Void> response = testRestTemplate.exchange(
                getBasePath() + "/case/" + caseId + "/stage/" + stageUUID + "/caseType/" + caseType + "/action",
                POST,
                new HttpEntity(requestBody, createValidAuthHeaders()),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private HttpHeaders createValidAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Auth-Groups", "/RERERCIiIiIiIiIiIiIiIg");
        headers.add("X-Auth-Userid", "a.person@digital.homeoffice.gov.uk");
        headers.add("X-Correlation-Id", "1");
        return headers;
    }

    private String getBasePath() {
        return "http://localhost:" + port;
    }

    private MockRestServiceServer buildMockService(RestTemplate restTemplate) {
        MockRestServiceServer.MockRestServiceServerBuilder infoBuilder = bindTo(restTemplate);
        infoBuilder.ignoreExpectOrder(true);
        return infoBuilder.build();
    }

}